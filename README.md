# ELF 16K Alignment Gradle Plugin [![Version](https://img.shields.io/gradle-plugin-portal/v/io.github.5peak2me.gradle.elf-16k-alignment?logo=gradle)](https://plugins.gradle.org/plugin/io.github.5peak2me.gradle.elf-16k-alignment)

[![Kotlin](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/elf-16k-alignment-gradle-plugin/main/gradle/libs.versions.toml&query=%24.versions.kotlin&label=Kotlin&color=blue&logo=kotlin)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/elf-16k-alignment-gradle-plugin/main/gradle/libs.versions.toml&query=%24.versions.agp&label=AGP&color=blue&logo=android)](https://developer.android.com/build/releases/gradle-plugin)
[![Gradle](https://img.shields.io/badge/dynamic/regex?url=https%3A%2F%2Fraw.githubusercontent.com%2F5peak2me%2Felf-16k-alignment-gradle-plugin%2Fmain%2Fgradle%2Fwrapper%2Fgradle-wrapper.properties&search=gradle-%28%5B0-9.%5D%2B%29-%28%3F%3Abin%7Call%29%5C.zip&replace=%241&label=Gradle&color=blue&logo=gradle&logoColor=white)](https://gradle.org)
[![Configuration Cache](https://img.shields.io/badge/Configuration%20Cache-supported-brightgreen.svg)](https://docs.gradle.org/current/userguide/configuration_cache.html)

English | [中文](README.zh_CN.md)

## 📖 Introduction

`elf-16k-alignment` is a Gradle plugin designed for Android developers to detect whether native libraries (JNI `.so` files) in project dependencies (AARs) comply with the **16KB page alignment** requirement.

Starting with Android 15, support for 16KB page sizes has been introduced. Applications containing native code must ensure their ELF files are aligned to 16KB boundaries; otherwise, they will fail to run on devices operating in 16KB mode.

This plugin automatically scans all AAR dependencies, identifies unaligned `.so` files, and generates detailed reports to help developers quickly locate issues and coordinate with SDK providers for fixes.

![Screenshot](assets/screenshot.png)

## ✨ Features

- 🔍 **Automated Scanning**: Automatically identifies AARs containing JNI libraries in runtime dependencies.
- 📏 **Alignment Detection**: Checks the `p_align` attribute of ELF files to verify 16KB (16384L) alignment.
- 🛡️ **RELRO Check**: Detects if `GNU_RELRO` is enabled for enhanced security.
- 📊 **Multi-format Reports**: Console summary output with warnings for unaligned libraries. Supports generating offline reports in CSV, HTML, and JSON formats.
- 🚀 **Build Integration**: Can be configured to run automatically during the build process (before `mergeNativeLibs`).
- 🎯 **Smart Filtering**: Option to only show unaligned libraries to reduce noise.
- 🛠️ **Quick Troubleshooting**: Provides `dependencyInsight` command suggestions for locating dependency sources.

## 🚀 Quick Start

### 1. Add Plugin to Project

Add the plugin to your root `build.gradle.kts`:

```kotlin
buildscript {
    dependencies {
        classpath("io.github.5peak2me.gradle:elf-16k-alignment:0.0.6")
    }
}
```

Or using the `plugins` DSL:

```kotlin
plugins {
    id("elf-16k-alignment") version "0.0.6"
}
```

Or using Version Catalog (`libs.versions.toml`):

In `gradle/libs.versions.toml`:

```toml
[plugins]
elf-alignment = { id = "elf-16k-alignment", version = "0.0.6" }
```

And in your root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.elf.alignment) apply false
}
```

### 2. Apply in App Module

Apply and configure the plugin in your `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("elf-16k-alignment")
}

elfAlignment {
    // Set desired max alignment (default is 16384L / 16KB)
    maxAlign.set(16384L)
    
    // Whether to run automatically on every Build (default is true)
    resolveOnBuild.set(true)
    
    // Configure output formats
    output {
        csv.set(true)
        html.set(true)
        json.set(true)
    }
}
```

### 3. Run Detection Task

You can manually execute the detection task:

```bash
./gradlew analyzeDebugAlignment
```

Reports will be generated in `app/build/reports/elf-16k-alignment/<variant>/`.

## 🔧 Configuration Parameters

| Parameter        | Type      | Default  | Description                                                                      |
|:-----------------|:----------|:---------|:---------------------------------------------------------------------------------|
| `maxAlign`       | `Long`    | `16384L` | Target alignment boundary in bytes. Android 15 recommends 16KB.                  |
| `resolveOnBuild` | `Boolean` | `true`   | Integration with build lifecycle. If `true`, runs before `mergeNativeLibs`.      |
| `output.csv`     | `Boolean` | `false`  | Generate CSV report.                                                             |
| `output.html`    | `Boolean` | `false`  | Generate HTML report.                                                            |
| `output.json`    | `Boolean` | `false`  | Generate JSON report.                                                            |

## 🤝 Contributing & Feedback

Welcome to submit Issues and Pull Requests!

- Issues: [Issues](https://github.com/5peak2me/elf-16k-alignment/issues)

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).
