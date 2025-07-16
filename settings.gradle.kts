pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ObjectScanner"
include(":app")

// Include the sceneform module
include(":sceneform")
project(":sceneform").projectDir = File("sceneformsrc/sceneform")

// Include the sceneformux module
include(":sceneformux")
project(":sceneformux").projectDir = File("sceneformux/ux")
