package com.techrove.timeclock.extensions

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSlider
import com.jfoenix.controls.JFXTextField
import com.jfoenix.controls.JFXToggleButton
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.decrypt
import com.techrove.timeclock.utils.Preference
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import tornadofx.*
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import java.util.prefs.Preferences


fun Color.withOpacity(opacity: Double): Color =
    deriveColor(0.0, 1.0, 1.0, opacity)


//fun EventTarget.jfxTextButton(text: String? = null, op: JFXTextField.() -> Unit = {}) {
//    addChildIfPossible(JFXTextField(text).apply { op(this) })
//}

fun EventTarget.jfxSlider(min: Double, max: Double, value: Double, op: JFXSlider.() -> Unit = {}) = JFXSlider(min, max, value).attachTo(this, op) {
}

fun EventTarget.jfxTextField(value: String? = null, op: JFXTextField.() -> Unit = {}) = JFXTextField().attachTo(this, op) {
    if (value != null) it.text = value
}
fun EventTarget.jfxTextField(property: ObservableValue<String>, op: JFXTextField.() -> Unit = {}) = jfxTextField().apply {
    bind(property)
    op(this)
}

fun EventTarget.jfxButton(text: String? = null, op: JFXButton.() -> Unit = {}) = JFXButton().attachTo(this, op) {
    it.text = text
}

fun EventTarget.jfxToggleButton(
    property: Property<Boolean>? = null,
    text: String? = null,
    op: JFXToggleButton.() -> Unit = {}
) = JFXToggleButton().attachTo(this, op) {
    it.text = text
    if (property != null) {
        it.selectedProperty().bindBidirectional(property)
    }
}

private val imageCache = mutableMapOf<String, Image>()

private fun imageEncrypted(key: ByteArray, file: String?): Image? {
    if (file == null) {
        return null
    }
    if (file.startsWith("\\") || file.startsWith("/")) {
        return Image(file)
    }
    try {
        return imageCache.getOrPut(file) {
            imageCache.clear()
            val path = URI.create(file).path
            File(path).decrypt(key, "photo")?.let {
                ByteArrayInputStream(it).buffered().use { stream ->
                    return@getOrPut Image(stream)
                }
            } ?: run {
                // try non-encrypted image next
                return@getOrPut Image(file, true)
            }
        }
    } catch (e: Exception) {
        return null
    }
}

fun EventTarget.imageViewEncrypted(file: String, op: ImageView.() -> Unit = {}) {
    imageview {
        imageProperty().value = imageEncrypted(Key.photoKey, file)
        op()
    }
}

fun EventTarget.imageViewEncrypted(property: ObservableValue<String?>, op: ImageView.() -> Unit = {}) {
    imageview {
        // NOTE: DO NOT use property.onChange due to performance issues.
        //property.onChange { setImageEncrypted(key, it) }
        imageProperty().bind(objectBinding(property) { imageEncrypted(Key.photoKey, value) })
        op()
    }
}

fun EventTarget.firstChildren(): Node? {
    return getChildList()?.first()
}

fun EventTarget.lastChildren(): Node? {
    return getChildList()?.last()
}

fun EventTarget.findChildOfId(id: String): Node? {
    getChildList()?.let { children ->
        children.find { it.id == id }?.let {
            return it
        } ?: run {
            children.forEach {
                it.findChildOfId(id)?.let {
                    return it
                }
            }
        }
    }
    return null
}

fun EventTarget.hasTextField(): Boolean {
    getChildList()?.let { children ->
        children.find { it is TextField }?.let {
            return true
        } ?: run {
            children.forEach {
                it.hasTextField().takeIf { it }?.let {
                    return true
                }
            }
        }
    }
    return false
}

fun TextField.getLastTextField(): Node? {
    return findParentOfType(Fieldset::class)?.lastChildren()?.lastChildren()?.firstChildren()
}

fun ObservableBooleanValue.onChangeTrue(op: () -> Unit) = apply {
    addListener { _, _, new -> if (new == true) op() }
}

private val userPref by lazy { Preferences.userNodeForPackage(Preference::class.java) }

fun Component.configIntegerProperty(key: String, defaultValue: Int = 0): SimpleIntegerProperty {
    return SimpleIntegerProperty(userPref.getInt(key, defaultValue)).apply {
        onChange { userPref.putInt(key, it) }
    }
}

fun Component.configStringProperty(key: String, defaultValue: String = ""): SimpleStringProperty {
    return SimpleStringProperty(userPref.get(key, defaultValue)).apply {
        onChange { userPref.put(key, it) }
    }
}

fun Component.configBooleanProperty(
    key: String,
    defaultValue: Boolean = false
): SimpleBooleanProperty {
    return SimpleBooleanProperty(userPref.getBoolean(key, defaultValue)).apply {
        onChange {
            userPref.putBoolean(key, it)
        }
    }
}
