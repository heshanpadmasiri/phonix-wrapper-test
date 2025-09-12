// Root project configuration for multi-project build

// Configure tasks that should delegate to the native subproject
tasks.register("buildNative") {
    description = "Build the native subproject"
    dependsOn(":native:build")
}

// Create a build task for the root project that builds native first, then ballerina
tasks.register("build") {
    description = "Build all subprojects (native first, then ballerina)"
    dependsOn(":native:build", ":ballerina:build")
}

// Create custom run task that builds native jar and runs ballerina
tasks.register("run") {
    description = "Build native jar and run the ballerina application"
    dependsOn(":native:jar", ":ballerina:build")
    doLast {
        exec {
            workingDir = file("ballerina")
            commandLine("bal", "run")
        }
    }
}

tasks.register("clean") {
    description = "Clean all subprojects"
    dependsOn(":native:clean", ":ballerina:clean")
}
