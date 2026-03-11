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
}

var mapsApiKey = ""

buildkonfig {
    packageName = "fyi.tono.stroppark"

    defaultConfigs {
        val localProperties = Properties()
        localProperties.load(rootProject.file("local.properties").inputStream())

        mapsApiKey = localProperties["MAP_API_KEY"].toString()

        buildConfigField(STRING, "API_BASE_URL", "https://ocm.tono.fyi")
        buildConfigField(STRING, "API_KEY", localProperties["OCM_API_KEY"].toString())
        buildConfigField(STRING, "MAPS_API_KEY", mapsApiKey)

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

            //GMaps
            implementation(libs.kmp.maps.compose)
            implementation(libs.kmp.maps.compose.utils)
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
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

