import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebase.crashlytics)
}

buildkonfig {
    packageName = "fyi.tono.stroppark"

    defaultConfigs {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")

        // Only try to load if the file actually exists
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }

        buildConfigField(STRING, "API_BASE_URL", "https://ocm.tono.fyi")

        // Priority: 1. Environment Variable (CI) 2. local.properties (Local) 3. Empty fallback
        val apiKey = System.getenv("OCM_API_KEY")
            ?: localProperties["OCM_API_KEY"]?.toString()
            ?: ""

        buildConfigField(STRING, "API_KEY", apiKey)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)

            //Ktor
            implementation(ktorLibs.client.okhttp)

            //Koin
            implementation(libs.koin.android)

            implementation(libs.play.services.location)
            implementation(libs.play.services.coroutines)

            //Crashlytics
            implementation(project.dependencies.platform(libs.firebase.bom))

            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.analytics)
        }
        iosMain.dependencies {
            implementation(ktorLibs.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material)
            implementation(libs.compose.material.icons.extended)

            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.navigation)
            implementation(libs.kotlinx.datetime)

            //Ktor
            implementation(ktorLibs.client.core)
            implementation(ktorLibs.client.logging)
            implementation(ktorLibs.client.contentNegotiation)
            implementation(ktorLibs.serialization.kotlinx.json)


            //Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            //Logging (not mandatory but otherwise gives annoying non-crashing error in log)
            implementation(libs.slf4j.simple)

            //Logger
            implementation(libs.kermit)

            //Moko
            implementation(libs.permissions.location)
            implementation(libs.permissions.compose)

            //Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(ktorLibs.client.mock)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        androidUnitTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.junit)
        }
    }
}

// Grab the run number, defaulting to 1 for local builds
val runNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "1"

// Use the tag from release-please if available, otherwise fallback to 1.0.X
val ciVersionName = System.getenv("VERSION_NAME") ?: "1.0.$runNumber"
val ciVersionCode = runNumber.toIntOrNull() ?: 1

android {
    namespace = "fyi.tono.stroppark"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }


    defaultConfig {
        applicationId = "fyi.tono.stroppark"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = ciVersionCode
        versionName = ciVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)

    debugImplementation(libs.androidx.ui.test.manifest)

    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

