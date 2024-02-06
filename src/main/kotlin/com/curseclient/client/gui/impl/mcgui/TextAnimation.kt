package com.curseclient.client.gui.impl.mcgui

class TextAnimation(private val texts: List<String>, private val delay: Int) {
    var output = ""

    init {
        start()
    }

    private fun start() {
        Thread {
            try {
                var index = 0
                while (true) {
                    for (i in 0 until texts[index].length) {
                        output += texts[index][i]
                        Thread.sleep(100)
                    }
                    Thread.sleep(delay.toLong())
                    for (i in output.length downTo 0) {
                        output = output.substring(0, i)
                        Thread.sleep(60)
                    }
                    if (index >= texts.size - 1) {
                        index = 0
                    }
                    index += 1
                    Thread.sleep(400)
                }
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }.start()
    }
}