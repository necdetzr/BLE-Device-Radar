plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.necdetzr.model"
}
dependencies {
    implementation(libs.kotlinx.serialization.core)
}
