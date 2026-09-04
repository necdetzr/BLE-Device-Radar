plugins {
    alias(libs.plugins.bledeviceradar.android.application)
    alias(libs.plugins.bledeviceradar.android.application.compose)
    alias(libs.plugins.bledeviceradar.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.necdetzr.bledeviceradar"
    defaultConfig {
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.navigation3)
    implementation(libs.androidx.core.splashscreen)

    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))

    implementation(libs.androidx.navigation3.runtime)
    implementation(project(":feature:radar"))
    implementation(project(":feature:history"))
    implementation(project(":feature:settings"))

}
