// build.gradle.kts (корневой файл)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false // ✅ ПРАВИЛЬНО: через alias
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false

}

