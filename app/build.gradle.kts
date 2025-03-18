plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.provajava"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.provajava"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "eWallet-v1.0")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/INDEX.LIST")
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.room.runtime.v251)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.gridlayout)
    implementation(libs.apache.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.xmlbeans)
    implementation(libs.play.services.auth.v2050)
    implementation(libs.google.api.client.android.v1332)
    implementation("com.google.apis:google-api-services-drive:v3-rev20250122-2.0.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")

    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    annotationProcessor(libs.androidx.room.compiler.v251)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
