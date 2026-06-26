plugins {
    `kotlin-dsl`
}

group = "com.necdetzr.bledeviceradar"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    implementation(libs.truth)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)


}

gradlePlugin {
    plugins{
        register("androidApplicationCompose"){
            id =libs.plugins.bledeviceradar.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication"){
            id = libs.plugins.bledeviceradar.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryCompose"){
            id = libs.plugins.bledeviceradar.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary"){
            id = libs.plugins.bledeviceradar.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature"){
            id = libs.plugins.bledeviceradar.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidRoom"){
            id = libs.plugins.bledeviceradar.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidTest"){
            id = libs.plugins.bledeviceradar.android.test.get().pluginId
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("jvmLibrary"){
            id = libs.plugins.bledeviceradar.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("root"){
            id = libs.plugins.bledeviceradar.root.get().pluginId
            implementationClass = "RootPlugin"
        }
        register("hilt"){
            id = libs.plugins.bledeviceradar.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
    }
}
