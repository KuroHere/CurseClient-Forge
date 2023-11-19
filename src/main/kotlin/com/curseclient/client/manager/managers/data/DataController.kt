package com.curseclient.client.manager.managers.data

import java.io.File

open class DataController(val name: String, val file: File) {
    open fun onWrite(){}
    open fun onLoad(){}
}