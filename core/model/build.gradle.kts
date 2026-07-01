plugins {
    alias(libs.plugins.bledeviceradar.android.library)
}

android {
    namespace = "com.necdetzr.model"
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}
