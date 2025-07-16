package com.yantraman.objectscanner

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.DisplayMetrics
import android.util.Log
import android.view.PixelCopy
import android.view.WindowInsets
import android.view.WindowMetrics
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var resultTextView: TextView
    private lateinit var btnAnalyze: Button
    private lateinit var scannerLine: ImageView
    private val markerPositions = mutableListOf<Vector3>()
    private var isMeasurementStarted = false
    private lateinit var tts: TextToSpeech
    private lateinit var animator: ValueAnimator

    private lateinit var interstitialAd: InterstitialAd
    private var isAdLoaded = false


    private val handler = Handler(Looper.getMainLooper())
    private var isProcessing = false

    private val processingRunnable = object : Runnable {
        override fun run() {
            if (isProcessing) {
                speakText(resultTextView.text.toString(), "slow")
                handler.postDelayed(this, 2000) // Repeat every 2 seconds
            }
        }
    }

    private fun startProcessingTTS() {
        isProcessing = true
        handler.post(processingRunnable)
    }

    private fun stopProcessingTTS() {
        isProcessing = false
        handler.removeCallbacks(processingRunnable)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        resultTextView = findViewById(R.id.resultTextView)
        scannerLine = findViewById(R.id.scannerLine)
        btnAnalyze = findViewById(R.id.btnAnalyze)

        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.setOnTapArPlaneListener { hitResult, _, _ -> onTreeDetected(hitResult) }
        btnAnalyze.setOnClickListener { analyzeTree() }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.getDefault()
                speakWelcomeText()
            }
        }

        val screenHeight = getScreenHeight().toFloat()
        // Initialize animation
        animator = ValueAnimator.ofFloat(0f, screenHeight).apply {
            duration = 2000  // Adjust speed
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                scannerLine.translationY = animation.animatedValue as Float
            }
        }

        loadInterstitialAd()

    }


    override fun onPause() {
        super.onPause()
        stopProcessingTTS()
        tts.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        stopTTS()
    }


    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            getString(R.string.gms_ads_interstitial_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoaded = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Interstitial ad failed to load: ${adError.message}")
                    isAdLoaded = false
                }
            }
        )
    }

    private fun showInterstitialAds() {
        if (isAdLoaded) {
            Toast.makeText(this, getString(R.string.ad_will_be_shown_shortly), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({

                interstitialAd.show(this)
                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        // Load next ad after the current one is dismissed
                        loadInterstitialAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdMob", "Interstitial ad failed to show: ${adError.message}")
                    }
                }
            }, 2000) // 2 seconds delay
        }
    }


    private fun getScreenHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+)
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            // For older Android versions (API 27-29)
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    private fun speakWelcomeText(speed: String = "slow") {
        val welcomeText = resultTextView.text.toString()
        speakText(welcomeText, speed)
    }

    private fun stopTTS() {
        tts.stop()
        tts.shutdown()
    }


    private fun speakText(text: String, speed: String = "medium") {
        val cleanedText = text.replace(Regex("[^\\p{L}\\p{N}\\s.,]"), "") // Remove emojis & special characters

        val speechRate = when (speed.lowercase()) {
            "slow" -> 0.6f    // Slow speed
            "medium" -> 0.7f  // Default (Normal)
            else -> 0.8f      // Fallback to Medium
        }

        tts.setSpeechRate(speechRate)
        tts.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    private fun speakResultText(text: String, speed: String = "medium") {
        val cleanedText = text.replace(Regex("[^\\p{L}\\p{N}\\s.,]"), "") // Remove emojis & special characters

        val speechRate = when (speed.lowercase()) {
            "slow" -> 0.6f    // Slow speed
            "medium" -> 0.7f  // Default (Normal)
            else -> 0.8f      // Fallback to Medium
        }

        tts.setSpeechRate(speechRate)
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "analyze_complete")
        tts.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, params, "analyze_complete")
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // TTS has started speaking
            }

            override fun onDone(utteranceId: String?) {
                // Log.e("TTS", "onDone in TTS")
                runOnUiThread {
                    // Show the ad only after TTS completes
                    showInterstitialAds()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                // Log.e("TTS", "Error in TTS")
            }
        })
    }


    private fun onTreeDetected(hitResult: HitResult) {
        if (!isMeasurementStarted) {
            isMeasurementStarted = true
            markerPositions.clear()
            // Toast.makeText(this, getString(R.string.tap_around_the_object_to_mark_points), Toast.LENGTH_SHORT).show()
        }
        placeMeasurementPoint(hitResult.createAnchor())
    }

    private fun placeMeasurementPoint(anchor: Anchor) {
        val anchorNode = AnchorNode(anchor).apply { setParent(arFragment.arSceneView.scene) }
        val pointNode = TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            select()
        }
        markerPositions.add(pointNode.worldPosition)
    }

    private fun analyzeTree() {
        scannerLine.visibility = ProgressBar.VISIBLE
        animator.start()
        resultTextView.text = getString(R.string.processing)
        startProcessingTTS()
        val circumference = calculateCircumference()
        captureTreeImage { bitmap ->
            val resizedBitmap = resizeBitmap(bitmap)
            lifecycleScope.launch {
                val result = GeminiAIHelper.analyzeImage(getString(R.string.ai_key), resizedBitmap, circumference)
                stopProcessingTTS()
                resultTextView.text = result
                animator.cancel()
                scannerLine.visibility = ProgressBar.GONE
                isMeasurementStarted = false



                speakResultText(resultTextView.text.toString())

            }
        }
    }


    private fun resizeBitmap(original: Bitmap): Bitmap {
        val scaleWidth = 512.toFloat() / original.width
        val scaleHeight = 512.toFloat() / original.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, false)
    }

    private fun calculateCircumference(): Float {
        var circumference = 0f
        for (i in markerPositions.indices) {
            val start = markerPositions[i]
            val end = markerPositions[(i + 1) % markerPositions.size]
            circumference += Vector3.subtract(end, start).length()
        }
        return circumference
    }

    private fun captureTreeImage(callback: (Bitmap) -> Unit) {
        val sceneView = arFragment.arSceneView
        val surfaceView = sceneView.renderer?.surfaceView

        if (surfaceView == null) {
            callback(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            return
        }

        val bitmap = Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(surfaceView, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                callback(bitmap)
            }
        }, Handler(Looper.getMainLooper()))
    }


}
