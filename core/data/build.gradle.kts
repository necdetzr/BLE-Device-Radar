plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.hilt)
}

android {
    namespace = "com.necdetzr.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))
    implementation(project(":core:ble"))
    implementation(project(":core:database"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)

}
