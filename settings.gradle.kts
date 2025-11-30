pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
        id("io.quarkus.extension") version quarkusPluginVersion
    }
}

include(":runtime", ":deployment")

rootProject.name="quarkus-reactive-repository"