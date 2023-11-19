package com.curseclient.client.module.modules.misc

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module


/**
 * @see MixinWorld.getThunderStrengthHead
 * @see MixinWorld.getRainStrengthHead
 */
object AntiWeather : Module(
    "AntiWeather",
    "Removes rain and thunder from your world",
    Category.MISC
)