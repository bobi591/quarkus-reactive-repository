
plugins {
    id("com.diffplug.spotless") version "7.2.1"
    id("java-library")
    id("maven-publish")
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-undertow-deployment")
    implementation(project(":runtime"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))

    implementation("io.quarkus.gizmo:gizmo:1.9.0")
    implementation("io.smallrye:jandex:3.5.2")

    implementation("io.quarkus:quarkus-hibernate-reactive")
    implementation("io.quarkus:quarkus-hibernate-reactive-deployment")
    implementation("io.quarkus:quarkus-reactive-datasource-deployment")
    implementation("io.quarkus:quarkus-hibernate-orm-deployment")
    implementation("io.quarkus:quarkus-caffeine-deployment")
    implementation("io.quarkus:quarkus-datasource-deployment")

    testImplementation("io.quarkus:quarkus-junit5-internal")
}

spotless {
    java {
        target("**/*.java")
        googleJavaFormat("1.28.0")
        removeUnusedImports()
        formatAnnotations()
    }
}