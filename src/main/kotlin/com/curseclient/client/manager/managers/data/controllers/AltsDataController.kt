package com.curseclient.client.manager.managers.data.controllers

import com.curseclient.CurseClient
import com.curseclient.client.gui.impl.mainmenu.elements.alt.Account
import com.curseclient.client.manager.managers.data.DataController
import com.curseclient.client.manager.managers.data.DataManager
import java.io.*
import java.nio.file.Files

// Lazy to fix (～￣▽￣)～
object AltsDataController: DataController(
    "AltsDataController",
    File(CurseClient.DIR + "/alts.txt")
) {

    override fun onLoad() {
        if (!file.exists()) {
            file.createNewFile()
        } else {
            readAlts()
        }
    }

    override fun onWrite() {
        try {
            val builder = StringBuilder()
            for (alt in DataManager.alts) {
                builder.append(alt.accountName + ":" + alt.dateAdded).append("\n")
            }
            Files.write(file.toPath(), builder.toString().toByteArray())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun readAlts() {
        try {
            val reader = BufferedReader(InputStreamReader(DataInputStream(FileInputStream(file.absolutePath))))
            var line: String

            while ((reader.readLine().also { line = it }) != null) {
                val parts = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val username = parts[0]
                DataManager.alts.add(Account(username, parts[1].toLong()))
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}