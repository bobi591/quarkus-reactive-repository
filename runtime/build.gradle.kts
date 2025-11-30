plugins {
    id("io.quarkus.extension")
    id("com.diffplug.spotless") version "7.2.1"
    id("java-library")
    id("maven-publish")
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-undertow")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("io.quarkus:quarkus-extension-processor:${quarkusPlatformVersion}"))

    implementation("io.quarkus:quarkus-hibernate-reactive")
}

quarkusExtension {
    deploymentModule.set("sidekick-reactive-repository:deployment")
}

spotless {
    java {
        target("**/*.java")
        googleJavaFormat("1.28.0")
        removeUnusedImports()
        formatAnnotations()
    }
}