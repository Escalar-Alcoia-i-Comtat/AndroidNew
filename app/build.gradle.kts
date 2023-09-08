import java.time.LocalDateTime
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "org.escalaralcoiaicomtat.android"
    compileSdk = 34

    val versionPropsFile = project.rootProject.file("version.properties")
    if (!versionPropsFile.canRead()) {
        throw GradleException("Cannot read version.properties")
    }
    val versionProps = Properties().apply {
        versionPropsFile.inputStream().use {
            load(versionPropsFile.inputStream())
        }
    }
    val code = versionProps.getProperty("VERSION_CODE").toInt()
    val version = versionProps.getProperty("VERSION_NAME")

    defaultConfig {
        applicationId = "org.escalaralcoiaicomtat.android"
        minSdk = 24
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
            (version.contains("dev") || version.contains("beta")).toString()
        )
    }

    signingConfigs {
        create("release") {
            val properties = Properties()
            project.rootProject.file("local.properties").inputStream().use(properties::load)

            val signingKeystorePassword: String = properties.getProperty("signingKeystorePassword")
            val signingKeyAlias: String = properties.getProperty("signingKeyAlias")
            val signingKeyPassword: String = properties.getProperty("signingKeyPassword")

            storeFile = File(project.rootDir, "keystore.jks")
            storePassword = signingKeystorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "PRODUCTION", "false")
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
        kotlinCompilerExtensionVersion = "1.5.3"
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

task("increaseVersionCode") {
    doFirst {
        val versionPropsFile = project.rootProject.file("version.properties")
        if (!versionPropsFile.canRead()) {
            throw GradleException("Cannot read version.properties")
        }
        val versionProps = Properties().apply {
            versionPropsFile.inputStream().use {
                load(versionPropsFile.inputStream())
            }
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

val roomVersion by project.properties
val workVersion by project.properties
val ktorVersion by project.properties

dependencies {
    implementation("androidx.activity:activity-ktx:1.8.0-alpha07")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.1")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // Jetpack Compose - Reorderable lists
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // Jetpack Compose - Zoomable Image
    implementation("net.engawapg.lib:zoomable:1.5.1")

    // Coil Image loading
    // implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Jetpack Compose - Rich Editor
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-beta03")

    // Room Database
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("androidx.work:work-multiprocess:$workVersion")

    // Ktor Client
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // KotlinX JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Allow using Java 8 features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Logging library
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Kotlin Reflection
    implementation(kotlin("reflect"))

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
