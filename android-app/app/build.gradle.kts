import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Lee local.properties (NO se sube a control de versiones) para no hardcodear
// la URL del backend en el código fuente.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
// Si no defines DEBUG_BASE_URL/RELEASE_BASE_URL en local.properties, cae a estos
// valores por defecto para que el proyecto siga compilando sin configuración extra.
val debugBaseUrl = (localProps.getProperty("DEBUG_BASE_URL") ?: "http://10.0.2.2:8080/")
val releaseBaseUrl = (localProps.getProperty("RELEASE_BASE_URL")
    ?: "https://CAMBIA-ESTO-por-tu-URL-de-Render.onrender.com/")

android {
    namespace = "com.plazaorbita.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.plazaorbita.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    buildTypes {
        debug {
            // Build de desarrollo: apunta al emulador (10.0.2.2) o a la IP de tu PC,
            // configurable en local.properties como DEBUG_BASE_URL=...
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            // Build de producción: apunta a tu backend en Render (HTTPS obligatorio),
            // configurable en local.properties como RELEASE_BASE_URL=...
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Red: Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Guardar sesión (token JWT)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Iconos vectoriales (outline) para la barra inferior y las pantallas de detalle
    implementation("androidx.compose.material:material-icons-extended")
}
