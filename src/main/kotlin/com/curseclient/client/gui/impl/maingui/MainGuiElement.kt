package com.curseclient.client.gui.impl.maingui

import com.curseclient.client.utility.render.vector.Vec2i
import com.curseclient.client.utility.render.HoverUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen

open class MainGuiElement(val xCentered: Int, val yCentered: Int, val width: Int, val height: Int) {
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
        return Vec2i(xCentered - width / 2, yCentered + height / 2)
    }

    protected fun getLeftTop(): Vec2i {
        return Vec2i(xCentered - width / 2, yCentered - height / 2)
    }
    protected fun getRightBottom(): Vec2i {
        return Vec2i(xCentered + width / 2, yCentered + height / 2)
    }

    protected fun getRightTop(): Vec2i {
        return Vec2i(xCentered + width / 2, yCentered - height / 2)
    }
}