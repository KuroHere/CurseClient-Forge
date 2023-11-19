package com.curseclient.client.utility.render.esp

import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d

class ESPBox (val pos1: Vec3d, val pos2: Vec3d) {
    val minX get() = pos1.x
    val minY get() = pos1.y
    val minZ get() = pos1.z

    val maxX get() = pos2.x
    val maxY get() = pos2.y
    val maxZ get() = pos2.z
}

fun ESPBox.move(x: Double, y: Double, z: Double) =
    move(Vec3d(x, y, z))

fun ESPBox.move(vec: Vec3d) =
    ESPBox(pos1.add(vec), pos2.add(vec))

fun AxisAlignedBB.toESPBox() =
    ESPBox(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))

fun ESPBox.center() =
    Vec3d((minX + maxX) * 0.5, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5)

fun ESPBox.toAxisAlignedBB() =
    AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)