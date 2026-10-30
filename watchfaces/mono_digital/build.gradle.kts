plugins { alias(libs.plugins.android.application) }

android {
    namespace = "com.chrona.watchfacepush.mono_digital"
    compileSdk = 36
    defaultConfig {
        // Watch Face Push requires <marketplace app id>.watchfacepush.<face id>
        applicationId = "com.chrona.watchfacepush.mono_digital"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes { release { isMinifyEnabled = false } }
    // Watch Face Push validator rejects anything outside /AndroidManifest.xml, /resources.arsc, /res/**, /META-INF/**
    packaging { resources.excludes += setOf("**/*.kotlin_module", "META-INF/*.version", "DebugProbesKt.bin") }
}
