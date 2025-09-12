plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.graalvm.python") version "24.2.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("org.graalvm.polyglot:polyglot:24.2.2")
    implementation("org.graalvm.polyglot:python:24.2.2")
}

application {
    // Define the main class for the application.
    mainClass = "com.example.App"
}

graalPy {
    packages = setOf("arize-phoenix-otel", "openinference-instrumentation-openai", "openai>=1.0.0", "grpcio==1.66.1")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
    }
    jar {
        enabled = false
        dependsOn(shadowJar)
    }
    build {
        dependsOn(shadowJar)
    }
    assemble {
        dependsOn(shadowJar)
    }
    distTar {
        dependsOn(shadowJar)
    }
    distZip {
        dependsOn(shadowJar)
    }
    startScripts {
        dependsOn(shadowJar)
    }
    // Override the run task to do nothing since we only want to build the jar
    named("run") {
        doFirst {
            println("Native run task skipped - only building JAR")
        }
        actions.clear()
    }
}