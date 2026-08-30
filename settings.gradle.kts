pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "BLE Device Radar"
include(":app")
include(":core:designsystem")
include(":core:common")
include(":core:ble")
include(":core:model")
include(":core:datastore")
include(":core:data")
include(":feature:radar")
include(":core:navigation")
include(":core:ui")
include(":feature:history")
include(":feature:settings")
include(":core:database")
include(":core:testing")
