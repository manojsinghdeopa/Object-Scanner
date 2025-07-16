package com.yantraman.objectscanner

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object GeminiAIHelper {


    suspend fun analyzeImage(key: String, bitmap: Bitmap, circumference: Float): String {
        val geminiModel = GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = key
        )

        val prompt = """
    Carefully analyze the given image.

    Step 1: Identify whether the main object in the image is a tree or not.
    - If it is a tree, proceed to Step 2.
    - If it is not a tree, provide a description of the detected object.

    Step 2: If the image contains a tree:
    - Identify the tree species with high accuracy.
    - Consider bark texture, leaf structure, branching pattern, and visible characteristics.
    - The tree's circumference is $circumference cm.
    - Estimate the tree's age based on species-specific growth rate factors.

    **Strict Response Format:**
    - Object Type: [Tree / Non-Tree]
    - If Tree:
        - Tree Species: [Exact Species Name], [Object Description with Brief useful details]
        - Confidence Score: [Number Only]%
        - Estimated Age: [Number Only] years
    - If Non-Tree:
        - Object Description: [Brief useful details]
""".trimIndent()


        return withContext(Dispatchers.IO) {
            try {
                val response = geminiModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                // Handle null or empty response text
                val responseText = response.text?.takeIf { it.isNotBlank() } ?: getRandomErrorMessage()
                parseGeminiResponse(responseText)
            } catch (e: Exception) {
                getRandomErrorMessage(e.message)
            }
        }
    }


    private fun parseGeminiResponse(responseText: String): String {
        val objectTypeRegex = "Object Type: (.+)".toRegex()
        val descriptionRegex = "Object Description: (.+)".toRegex()
        val speciesRegex = "Tree Species: (.+)".toRegex()
        val confidenceRegex = "Confidence Score: (\\d+)%".toRegex()
        val ageRegex = "Estimated Age: (\\d+) years".toRegex()

        val objectType = objectTypeRegex.find(responseText)?.groupValues?.get(1) ?: "Unknown"
        val description = descriptionRegex.find(responseText)?.groupValues?.get(1) ?: "No description available"

        return if (objectType.lowercase() == "tree") {
            val species = speciesRegex.find(responseText)?.groupValues?.get(1) ?: "Unknown"
            val confidence = confidenceRegex.find(responseText)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val age = ageRegex.find(responseText)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            """
                🌳 Tree Species: $species
                ✅ Confidence Score: $confidence%
                ⏳ Estimated Age: $age years
            """.trimIndent()
        } else {
            """
                $description
            """.trimIndent()
        }
    }


    private fun getRandomErrorMessage(error: String? = null): String {
        val randomIdea = ideas.random()
        return if (error != null) {
            "Error: 404. Fun Fact: $randomIdea"
        } else {
            "Error: 1003. Fun Fact: $randomIdea"
        }
    }

    private val ideas = listOf(
        "Did you know honey never spoils?",
        "Did you know octopuses have three hearts?",
        "Did you know bananas are berries, but strawberries aren't?",
        "Did you know a group of flamingos is called a 'flamboyance'?",
        "Did you know that water can boil and freeze at the same time?",
        "Did you know that sharks existed before trees?",
        "Did you know a day on Venus is longer than a year on Venus?",
        "Did you know wombat poop is cube-shaped?",
        "Did you know that there’s a species of jellyfish that can live forever?",
        "Did you know your brain generates enough electricity to power a light bulb?",
        "Did you know that cats have a special purring frequency that helps them heal faster?",
        "Did you know sloths can hold their breath longer than dolphins?",
        "Did you know a bolt of lightning is five times hotter than the sun’s surface?",
        "Did you know turtles can breathe through their butts?",
        "Did you know a flea can jump 350 times its body length?",
        "Did you know the Eiffel Tower grows taller in summer due to heat expansion?",
        "Did you know astronauts can’t burp in space?",
        "Did you know there are more trees on Earth than stars in the Milky Way?",
        "Did you know bees can recognize human faces?",
        "Did you know humans share about 60% of their DNA with bananas?",
        "Did you know a shrimp's heart is located in its head?",
        "Did you know the longest hiccuping spree lasted 68 years?",
        "Did you know an octopus can squeeze through anything larger than its beak?",
        "Did you know Venus is the hottest planet in our solar system, not Mercury?",
        "Did you know sea otters hold hands while sleeping to avoid drifting apart?",
        "Did you know goldfish can recognize their owners?",
        "Did you know cows have best friends and get stressed when separated?",
        "Did you know jellyfish are 95% water?",
        "Did you know elephants can 'hear' with their feet?",
        "Did you know a blue whale's heart is the size of a small car?",
        "Did you know that bats always turn left when exiting a cave?",
        "Did you know the word 'alphabet' comes from the first two Greek letters, Alpha and Beta?",
        "Did you know a single cloud can weigh as much as a million pounds?",
        "Did you know butterflies can taste with their feet?",
        "Did you know hot water freezes faster than cold water?",
        "Did you know you can’t hum while holding your nose?",
        "Did you know rubber bands last longer when refrigerated?",
        "Did you know a crocodile can’t stick out its tongue?",
        "Did you know that the inventor of the frisbee was turned into a frisbee after he died?",
        "Did you know your liver can regenerate even if 75% of it is removed?",
        "Did you know cheetahs can’t roar but can purr?",
        "Did you know armadillos can roll into a perfect ball?",
        "Did you know the human nose can detect over 1 trillion different scents?",
        "Did you know some turtles can live for more than 150 years?",
        "Did you know that the fingerprints of koalas are nearly identical to humans'?",
        "Did you know a strawberry isn’t actually a berry, but an avocado is?",
        "Did you know there’s a lake in Australia that stays bright pink?",
        "Did you know the world's largest desert is actually Antarctica?",
        "Did you know a day on Mars is just 37 minutes longer than a day on Earth?",
        "Did you know pineapples take about two years to grow?",
        "Did you know that dolphins have names for each other?",
        "Did you know humans shed about 600,000 skin particles every hour?",
        "Did you know a panda’s diet is 99% bamboo?",
        "Did you know watermelons originated in Africa?",
        "Did you know there’s a species of fish that can climb trees?",
        "Did you know the dot over a lowercase 'i' is called a 'tittle'?",
        "Did you know that all polar bears are left-handed?",
        "Did you know a group of crows is called a murder?",
        "Did you know an ant can carry 50 times its own body weight?",
        "Did you know the smell of freshly-cut grass is actually a plant distress signal?",
        "Did you know a giraffe’s tongue is blue-black and can be up to 20 inches long?",
        "Did you know male seahorses carry and give birth to their young?",
        "Did you know peanuts aren’t nuts but legumes?",
        "Did you know some frogs can freeze and then thaw back to life?",
        "Did you know a jellyfish’s mouth is also its anus?",
        "Did you know snails can sleep for up to 3 years?",
        "Did you know the speed of a sneeze can reach up to 100 mph?",
        "Did you know the Great Wall of China is not actually visible from space?",
        "Did you know there are more fake flamingos in the world than real ones?",
        "Did you know lightning strikes the Earth 100 times per second?",
        "Did you know the human stomach gets a new lining every few days to prevent digestion of itself?",
        "Did you know the average cloud weighs over 1 million pounds?",
        "Did you know the moon is slowly drifting away from Earth?",
        "Did you know sloths only poop once a week?",
        "Did you know cotton candy was invented by a dentist?",
        "Did you know blue whales eat up to 8,000 pounds of food per day?",
        "Did you know birds don’t urinate?",
        "Did you know it takes about 8 minutes for sunlight to reach Earth?",
        "Did you know camels have three eyelids to protect against sand?",
        "Did you know wombats have cube-shaped poop?",
        "Did you know it’s impossible to lick your elbow?",
        "Did you know a hippo's sweat is pink?",
        "Did you know that glass is actually a very slow-moving liquid?",
        "Did you know there’s a type of lizard that can run on water?",
        "Did you know it rains diamonds on Jupiter and Saturn?",
        "Did you know that a bolt of lightning is six times hotter than the sun’s surface?",
        "Did you know a single teaspoon of honey represents the life’s work of 12 bees?",
        "Did you know you are about 1 cm taller in the morning than at night?",
        "Did you know it’s physically impossible for pigs to look up at the sky?",
        "Did you know the smell of chocolate increases brain waves, making you feel relaxed?",
        "Did you know that slugs have four noses?",
        "Did you know the heart of a shrimp is located in its head?",
        "Did you know there are more stars in the universe than grains of sand on all the Earth’s beaches?"
    )


}

