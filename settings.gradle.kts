pluginManagement {
    repositories {
        google()
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

rootProject.name = "TOSS"
include(":apps:toss-home")
include(":apps:toss-control")
include(":core:capabilities")
include(":core:projector-control")
include(":devices:hcs350:android")
