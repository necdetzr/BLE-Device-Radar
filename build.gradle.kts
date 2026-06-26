import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false

    id("dev.detekt") version "2.0.0-alpha.3"
}

detekt {
    toolVersion = "2.0.0-alpha.3"
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

subprojects {
    apply(plugin = "dev.detekt")

    tasks.withType<Detekt>().configureEach {
        reports {
            checkstyle.required.set(true)
            html.required.set(true)
            sarif.required.set(true)
            markdown.required.set(true)
        }
    }
}
