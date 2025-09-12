// Ballerina subproject build configuration

tasks.register("copyNativeJar") {
    description = "Copy the native JAR to ballerina lib directory"
    dependsOn(":native:jar")
    doLast {
        val libDir = file("lib")
        if (!libDir.exists()) {
            libDir.mkdirs()
        }
        
        val nativeJar = project(":native").tasks.getByName("jar").outputs.files.singleFile
        copy {
            from(nativeJar)
            into(libDir)
        }
        println("Copied ${nativeJar.name} to ballerina/lib/")
    }
}

tasks.register<Exec>("balBuild") {
    description = "Build the Ballerina project"
    dependsOn("copyNativeJar")
    workingDir = projectDir
    commandLine("bal", "build")
}

tasks.register("build") {
    description = "Build the ballerina subproject"
    dependsOn("balBuild")
}

tasks.register<Exec>("balClean") {
    description = "Clean the Ballerina project"
    workingDir = projectDir
    commandLine("bal", "clean")
    isIgnoreExitValue = true
}

tasks.register("clean") {
    description = "Clean the ballerina subproject"
    dependsOn("balClean")
    doLast {
        val libDir = file("lib")
        if (libDir.exists()) {
            libDir.deleteRecursively()
        }
    }
}