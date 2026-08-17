plugins {
    alias(libs.plugins.bledeviceradar.android.library)
}

android {
    namespace = "com.necdetzr.domain"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

}
