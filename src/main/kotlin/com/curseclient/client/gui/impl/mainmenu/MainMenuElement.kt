package com.curseclient.client.gui.impl.mainmenu

import com.curseclient.client.gui.impl.mainmenu.elements.button.IconButton
import com.curseclient.client.module.impls.client.MenuShader
import com.curseclient.client.module.impls.client.MenuShader.draw
import com.curseclient.client.utility.render.HoverUtils
import com.curseclient.client.utility.render.shader.RoundedUtil.drawImage
import com.curseclient.client.utility.render.vector.Vec2i
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

open class MainMenuElement(val x: Int, val y: Int, val width: Int, val height: Int) {
    protected var isHovered = false
    protected val mc: Minecraft = Minecraft.getMinecraft()

    protected open fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float, mainGui: GuiScreen){}
    open fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, mainGui: GuiScreen){}
    open fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int, mainGui: GuiScreen){}

    protected open fun onMouseEnter(){}
    protected open fun onMouseExit(){}


    fun onDraw(mouseX: Int, mouseY: Int, partialTicks: Float, mainGui: GuiScreen){
        onRender(mouseX, mouseY, partialTicks, mainGui)
        if(HoverUtils.isHovered(mouseX, mouseY, getLeftTop().x, getLeftTop().y, getRightBottom().x, getRightBottom().y)){
            if(!isHovered){
                isHovered = true
                onMouseEnter()
            }
        }else{
            if(isHovered){
                isHovered = false
                onMouseExit()
            }
        }
    }

    protected fun getLeftBottom(): Vec2i {
        return Vec2i(x - width / 2, y + height / 2)
    }

    protected fun getLeftTop(): Vec2i {
        return Vec2i(x - width / 2, y - height / 2)
    }
    protected fun getRightBottom(): Vec2i {
        return Vec2i(x + width / 2, y + height / 2)
    }

    protected fun getRightTop(): Vec2i {
        return Vec2i(x + width / 2, y - height / 2)
    }
}