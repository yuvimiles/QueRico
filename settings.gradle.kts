pluginManagement {
    repositories {
        google()  // פשטתי - אין צורך בהגבלות מורכבות
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

rootProject.name = "QueRico"
include(":app")