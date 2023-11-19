package com.curseclient.client.setting


open class Setting<T> (val name: String, val visibility: () -> Boolean, val description: String) {
    val isVisible get() = visibility()

    val listeners = ArrayList<() -> Unit>()
}