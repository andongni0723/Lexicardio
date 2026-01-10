package com.andongni.vcblearn.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.util.Locale

fun getAppVersion(context: Context, begin: String = ""): String {
    return try {
        val pm = context.packageManager
        val pkg = context.packageName
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0)
        }
        info.versionName?.let { begin + it } ?: "unknown version"
    } catch (e: PackageManager.NameNotFoundException) {
        "unknown"
    }
}

fun getAppName(context: Context): String {
    return context.applicationInfo.loadLabel(context.packageManager).toString()
}

fun compareVersion(a: String, b: String): Int {
    val pa = a.split(".")
    val pb = b.split(".")
    val max = maxOf(pa.size, pb.size)
    for (i in 0 until max) {
        val ai = pa.getOrNull(i)?.toIntOrNull() ?: 0
        val bi = pb.getOrNull(i)?.toIntOrNull() ?: 0
        if (ai != bi) return ai compareTo bi
    }
    return 0
}

fun bytesToMiB(bytes: Int): String =
    String.format(Locale.US, "%.2f", bytes / (1024.0 * 1024.0))
