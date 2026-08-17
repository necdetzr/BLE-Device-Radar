plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.necdetzr.model"
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)
}
