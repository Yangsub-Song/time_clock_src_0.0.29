package com.techrove.timeclock.controller.admin

import com.techrove.timeclock.controller.BaseController
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import tornadofx.asObservable
import tornadofx.toProperty
import java.io.File

/**
 * 관리자 설정 / 이미지 viewer controller
 */
class PictureController : BaseController() {

    val photoSelectedProperty = SimpleObjectProperty<File>()
    val photoListLoadingProperty = false.toProperty()
    val photoEmptyProperty = false.toProperty()
    val photoSortDescendingProperty = true.toProperty()

    fun loadPictures(): ObservableList<File> {
        photoEmptyProperty.value = false
        photoListLoadingProperty.value = true
        return File("./pictures").walk().toList()
            .filter { it.name.endsWith(".jpg", ignoreCase = true) }
            .sortedWith(if (photoSortDescendingProperty.value) reverseOrder() else naturalOrder())
            .let {
                it.asObservable().also {
                    if (it.isEmpty()) {
                        photoEmptyProperty.value = true
                    } else {
                        photoListLoadingProperty.value = false
                        photoSelectedProperty.value = it.getOrNull(0)
                    }
                }
            }
    }
}
