package com.curseclient.client.manager.managers.data.controllers.Alts

import com.curseclient.CurseClient
import com.curseclient.client.gui.impl.altmanager.Alt
import com.curseclient.client.gui.impl.altmanager.AltButton
import com.curseclient.client.gui.impl.altmanager.AltGui
import com.curseclient.client.manager.managers.data.DataController
import net.minecraft.client.gui.ScaledResolution
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object AltsDataController: DataController(
    "AltsDataController",
    File(CurseClient.DIR + "/alts.txt")
) {

    override fun onLoad() {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val writer = FileWriter(file)
            for (altButton in AltGui.altButtons) {
                writer.write(((altButton.email + ":" + altButton.password).toString() + ":" + altButton.altType.equals(Alt.AltType.MICROSOFT)).toString() + "\n")
            }
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onWrite() {
        try {
            if (file.exists()) {
                val scaledResolution = ScaledResolution(AltGui.mc)
                val width = scaledResolution.scaledWidth.toFloat()
                val x = width - 150.0f
                val reader = FileReader(file)
                val bufferedReader = BufferedReader(reader)
                bufferedReader.lines().forEach { line: String ->
                    val split = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    AltGui.altButtons.add(AltButton(split[0], split[1], if (split[2].toBoolean()) Alt.AltType.MICROSOFT else Alt.AltType.CRACKED, x + 5.0f, 25 + AltGui.altButtons.size * 50.0f, 140.0f, 45.0f))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}