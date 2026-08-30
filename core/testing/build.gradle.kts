plugins {
    alias(libs.plugins.bledeviceradar.android.library)
}

android {
    namespace = "com.necdetzr.testing"
}

dependencies {
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
