package com.curseclient.client.event.events.render

import com.curseclient.client.event.Cancellable
import net.minecraft.util.text.ITextComponent

class PreScreenshotEvent(var response: ITextComponent?) : Cancellable()