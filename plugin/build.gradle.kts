import org.gradle.plugin.compatibility.compatibility
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.plugin.publish)
}

group = "io.github.5peak2me.plugin.gradle"
version = "0.0.3"

// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    explicitApi()
}

fun Provider<PluginDependency>.toDep() = map {
    dependencyFactory.create(it.pluginId, "${it.pluginId}.gradle.plugin", it.version.toString())
}

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.kotlin.android.toDep())
    implementation(libs.picnic) {
        exclude("org.jetbrains.kotlin")
    }
}

gradlePlugin {
    website.set("https://daijinlin.com/elf-16k-alignment")
    vcsUrl.set("https://github.com/5peak2me/elf-16k-alignment")

    plugins {
        create("elf-16k-alignment") {
            id = "io.github.5peak2me.gradle.elf-16k-alignment"
            implementationClass = "ElfAlignmentPlugin"

            displayName = "elf-16k-alignment"
            description = "elf-16k-alignment is a Gradle plugin designed for Android developers to detect whether native libraries (JNI .so files) in project dependencies (AARs) comply with the 16KB page alignment requirement."
            tags.set(listOf("android gradle plugin elf-16k-alignment"))

            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}
