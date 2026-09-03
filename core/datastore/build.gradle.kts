plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.hilt)
}

android {
    namespace = "com.necdetzr.datastore"
}

dependencies {
    testImplementation(libs.junit)
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation(project(":core:model"))
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
