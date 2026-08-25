import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// GROQ_API_KEY dibaca dari local.properties agar tidak ikut ter-commit.
//
// CATATAN KEAMANAN: buildConfigField menaruh nilainya sebagai string biasa di
// dalam DEX, jadi siapa pun yang mengunduh APK bisa mengekstraknya. Ini bisa
// diterima selama masih prototipe; untuk rilis nyata, panggilan Groq harus
// pindah ke Supabase Edge Function agar kuncinya tidak pernah ada di perangkat.
// Kunci kosong bukan kegagalan: aplikasi memakai baseline berbasis aturan yang
// dapat diaudit, dan kartu prakiraan menyebut sumbernya apa adanya.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val groqApiKey: String = localProps.getProperty("GROQ_API_KEY", "")
// Model Groq bisa diganti tanpa menyentuh kode. Default memakai model yang
// masih tersedia di tier standar; llama-3.1-8b-instant yang disebut proposal
// dijadwalkan berhenti 16 Agustus 2026 dan kini hanya untuk akun Enterprise.
val groqModel: String = localProps.getProperty("GROQ_MODEL", "openai/gpt-oss-20b")
val supabaseUrl: String = localProps.getProperty("SUPABASE_URL", "")
val supabaseKey: String = localProps.getProperty("SUPABASE_PUBLISHABLE_KEY", "")

android {
    namespace = "com.example.alirinmobile"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.alirinmobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "GROQ_MODEL", "\"$groqModel\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            // Aktif supaya string dan nama kelas tidak terbaca telanjang di APK.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.osmdroid.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.play.services.location)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.auth)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.androidx.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
