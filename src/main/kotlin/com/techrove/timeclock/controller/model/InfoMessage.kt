package com.techrove.timeclock.controller.model

import com.techrove.timeclock.view.custom.IconType
import javafx.util.Duration
import tornadofx.seconds

/**
 * info popup 정보
 */
data class InfoMessage(
    val title: String,
    val message: String,
    val iconType: IconType = IconType.Info,
    val formMessages: Map<String, String>? = null,
    val errorMessage: String? = null,
    val imageFile: String? = null,
    val imageEncrypted: Boolean = true,
    val buttons: List<String>? = null,
    val delay: Duration? = 3.seconds,
)
