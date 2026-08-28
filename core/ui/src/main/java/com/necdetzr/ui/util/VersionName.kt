package com.necdetzr.ui.util

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun getAppVersionName(): String{
    val context = LocalContext.current
    return remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: DEFAULTVERSIONNAME
        } catch (_: PackageManager.NameNotFoundException) {
            DEFAULTVERSIONNAME
        }
    }
}

private const val DEFAULTVERSIONNAME = "1.0.0"
