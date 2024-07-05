// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.amazonappstorepublisher) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.sentry) apply false
}
