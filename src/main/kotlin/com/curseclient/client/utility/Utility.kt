package com.curseclient.client.utility

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val DATE_FORMAT =  SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")

fun getTimestampedPNGFileForDirectory(
    directory : File
) : File {
    val date = DATE_FORMAT.format(Date()).toString()
    var i = 1

    while(true) {
        val file = File(directory, "$date${if(i == 1) "" else "_$i"}.png")

        if(!file.exists()) {
            return file
        }

        ++i
    }
}