# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a GraalPy wrapper project that demonstrates embedding Python code within a Java application using GraalVM's polyglot capabilities. The project provides a minimal example of executing Python code from Java with security restrictions in place.

## Prerequisites

- GraalVM for JDK 24 must be installed and `JAVA_HOME` set accordingly

## Essential Commands

- **IMPORTANT: in mac os you may need to use a system version graalpy if you are running in to meson build issues**
  Example:

  ```bash
  ./gradlew build -Dgraalpy.vfs.venvLauncher=/Users/heshanp/GraalVM/graalpy-24.2.2-macos-aarch64/bin/graalpy
  ```

- **IMPORTANT: in mac os if you are running in to further build issues use the `./build-with-wrapper.sh`**
  - Some build commands due to some reason try to use `gcc` to compile c++ code. Also we need to make sure it uses `glibc` instead of MacOS version in some cases.
  - If it is breaking first make sure path to `g++` is correct.

### Build and Test

```bash
./gradlew build          # Build the entire project
./gradlew test           # Run tests
./gradlew clean          # Clean build directory
./gradlew jar            # Build JAR archive
```

### Run Application

```bash
./gradlew run            # Execute the main method
```

### Development Tasks

```bash
./gradlew classes        # Compile main classes only
./gradlew testClasses    # Compile test classes only
./gradlew assemble       # Assemble outputs without running tests
```

## Architecture

### Core Components

- **Main Application**: `src/main/java/com/example/App.java` - Single entry point that creates a GraalVM polyglot context for Python execution
- **GraalVM Context**: Configured with security restrictions including:
  - No native access
  - No thread creation
  - Restricted file system and network I/O access

### Dependencies

- `org.graalvm.polyglot:polyglot:24.2.2` - Core polyglot API
- `org.graalvm.polyglot:python:24.2.2` - Python language support

### Project Structure

- Single-module Gradle project using Kotlin DSL
- Standard Maven directory layout (`src/main/java`)
- Application plugin configured with main class `com.example.App`

## Key Configuration

- Project name: `graalpy-phonix-wrapper`
- Main class: `com.example.App`
- Build system: Gradle with Kotlin DSL
- Security model: Restrictive polyglot context configuration for safe Python execution

## Adding python dependencies

1. You need to make sure graapy plugin is there

```
plugins {
    id("org.graalvm.python") version "24.2.2"
}
```

2. Then just add the package as you would to `requirements.txt`

```
graalPy {
    packages = setOf("pyfiglet==1.0.2")
}
```
