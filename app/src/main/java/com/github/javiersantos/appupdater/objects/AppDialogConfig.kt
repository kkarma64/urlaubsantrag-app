package com.github.javiersantos.appupdater.objects

import android.content.Context

class AppDialogConfig(
    val context: Context,
    val title: String,
    val description: String,
    val btnUpdate: String,
    val btnDismiss: String,
    val btnDontShowAgain: String?
)
