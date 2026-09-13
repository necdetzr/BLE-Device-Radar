package com.necdetzr.bledeviceradar.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
internal fun rememberInAppReviewLauncher(): () -> Unit {
    val context = LocalContext.current
    val activity = context.findActivity()
    val applicationContext = context.applicationContext

    val reviewManager = remember(applicationContext) {
        ReviewManagerFactory.create(applicationContext)
    }

    return remember(activity, reviewManager) {
        {
            activity?.let { currentActivity ->
                reviewManager.requestReviewFlow()
                    .addOnCompleteListener { request ->
                        if (request.isSuccessful) {
                            reviewManager.launchReviewFlow(
                                currentActivity,
                                request.result,
                            )
                        }
                    }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
