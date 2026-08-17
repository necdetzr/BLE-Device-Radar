plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.hilt)
    alias(libs.plugins.bledeviceradar.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.necdetzr.database"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)
}
