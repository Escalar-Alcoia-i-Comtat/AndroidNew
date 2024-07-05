import java.time.LocalDateTime
import java.util.Properties

plugins {
    alias(libs.plugins.amazonappstorepublisher)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.sentry)
}

fun readPropertiesFile(filename: String): Properties {
    val file = project.rootProject.file(filename)
    if (!file.canRead()) throw GradleException("Cannot read $filename")
    return Properties().apply {
        file.inputStream().use {
            load(file.inputStream())
        }
    }
}

android {
    namespace = "org.escalaralcoiaicomtat.android"
    compileSdk = 34

    val versionProps = readPropertiesFile("version.properties")
    val code = versionProps.getProperty("VERSION_CODE").toInt()
    val version = versionProps.getProperty("VERSION_NAME")

    defaultConfig {
        applicationId = "org.escalaralcoiaicomtat.android"
        minSdk = 21
        targetSdk = 34
        versionCode = code
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "PROTOCOL", "\"https\"")
        buildConfigField("String", "HOSTNAME", "\"backend.escalaralcoiaicomtat.org\"")
        buildConfigField(
            "Boolean",
            "PRODUCTION",
            (!version.contains("dev") && !version.contains("beta")).toString()
        )
    }

    signingConfigs {
        create("release") {
            val properties = readPropertiesFile("local.properties")

            val signingKeystorePassword: String? = properties.getProperty("signingKeystorePassword")
            val signingKeyAlias: String? = properties.getProperty("signingKeyAlias")
            val signingKeyPassword: String? = properties.getProperty("signingKeyPassword")

            storeFile = File(project.rootDir, "keystore.jks")
            storePassword = signingKeystorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    buildTypes {
        configureEach {
            val properties = readPropertiesFile("local.properties")
            buildConfigField("String", "SENTRY_DSN", "\"${properties.getProperty("sentry_dsn")!!}\"")
        }
        debug {
            buildConfigField("Boolean", "PRODUCTION", "false")
            buildConfigField("String", "HOSTNAME", "\"beta-backend.escalaralcoiaicomtat.org\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

amazon {
    securityProfile = File(project.rootDir, "amazon_security_profile.json")
    applicationId = "org.escalaralcoiaicomtat.android"
    pathToApks = listOf(File(project.rootDir, "app/build/outputs/apk/release/app-release.apk"))
    replaceEdit = true
}

task("increaseVersionCode") {
    doFirst {
        val versionPropsFile = project.rootProject.file("version.properties")
        if (!versionPropsFile.canRead()) {
            throw GradleException("Cannot read version.properties")
        }
        val versionProps = Properties().apply {
            versionPropsFile.inputStream().use { load(it) }
        }
        val code = versionProps.getProperty("VERSION_CODE").toInt() + 1
        versionProps["VERSION_CODE"] = code.toString()
        versionPropsFile.outputStream().use {
            val date = LocalDateTime.now()
            versionProps.store(it, "Updated at $date")
        }
        println("Increased version code to $code")
    }
}

task("updateVersionName") {
    doFirst {
        val newVersionName = project.property("version") as String?
        if (newVersionName == null || newVersionName == "unspecified") {
            error("Please, specify a version with -Pversion=<version>")
        }

        val versionPropsFile = project.rootProject.file("version.properties")
        if (!versionPropsFile.canRead()) {
            throw GradleException("Cannot read version.properties")
        }
        val versionProps = Properties().apply {
            versionPropsFile.inputStream().use { load(it) }
        }
        versionProps["VERSION_NAME"] = newVersionName
        versionPropsFile.outputStream().use {
            val date = LocalDateTime.now()
            versionProps.store(it, "Updated at $date")
        }
        println("Updated version name to $newVersionName")
    }
}

dependencies {
    implementation(libs.androidx.activity.base)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.livedata)

    // Jetpack Compose
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.ui.base)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.compose.ui.googleFonts)
    implementation(libs.compose.material.iconsExtended)
    implementation(libs.compose.material3.base)
    implementation(libs.compose.material3.windowSizeClass)
    implementation(libs.compose.runtime.livedata)

    // Jetpack Compose - Reorderable lists
    implementation(libs.compose.reorderable)

    // Jetpack Compose - Zoomable Image
    implementation(libs.compose.zoomable)

    // Coil Image loading
    implementation(libs.compose.coil)

    // Jetpack Compose - Rich Editor
    implementation(libs.compose.richEditor)

    // Room Database
    implementation(libs.androidx.room.base)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // DataStore preferences
    implementation(libs.androidx.datastore)

    // WorkManager
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.multiprocess)

    // Ktor Client
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.serialization.kotlinx.json)

    // KotlinX JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // Allow using Java 8 features
    coreLibraryDesugaring(libs.android.desugar)

    // Logging library
    implementation(libs.timber)

    // Kotlin Reflection
    implementation(kotlin("reflect"))

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)

    androidTestImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.compose.ui.test.manifest)
}
