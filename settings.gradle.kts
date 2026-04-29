pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "placeholderapi"
            url = uri("https://repo.helpch.at/releases/")
        }
        mavenCentral()
    }
}

rootProject.name = "ZRestart"
