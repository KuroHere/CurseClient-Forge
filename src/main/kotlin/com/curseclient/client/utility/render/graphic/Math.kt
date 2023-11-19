package com.curseclient.client.utility.render.graphic

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor

const val PI_FLOAT: Float = 3.14159265358979323846f

const val FLOOR_DOUBLE_D: Double = 1_073_741_824.0
const val FLOOR_DOUBLE_I: Int = 1_073_741_824

const val FLOOR_FLOAT_F: Float = 4_194_304.0f
const val FLOOR_FLOAT_I: Int = 4_194_304

fun Double.floorToInt(): Int = floor(this).toInt()
fun Float.floorToInt(): Int = floor(this).toInt()

fun Double.ceilToInt(): Int = ceil(this).toInt()
fun Float.ceilToInt(): Int = ceil(this).toInt()

fun Double.fastFloor(): Int = (this + FLOOR_DOUBLE_D).toInt() - FLOOR_DOUBLE_I
fun Float.fastFloor(): Int = (this + FLOOR_FLOAT_F).toInt() - FLOOR_FLOAT_I
fun Double.fastCeil(): Int = FLOOR_DOUBLE_I - (FLOOR_DOUBLE_D - this).toInt()
fun Float.fastCeil(): Int = FLOOR_FLOAT_I - (FLOOR_FLOAT_F - this).toInt()

fun Float.toRadian(): Float = this / 180.0f * PI_FLOAT
fun Double.toRadian(): Double = this / 180.0 * PI

fun Float.toDegree(): Float = this * 180.0f / PI_FLOAT
fun Double.toDegree(): Double = this * 180.0 / PI

inline val Double.sq: Double get() = this * this
inline val Float.sq: Float get() = this * this
inline val Int.sq: Int get() = this * this

inline val Double.cubic: Double get() = this * this * this
inline val Float.cubic: Float get() = this * this * this
inline val Int.cubic: Int get() = this * this * this

inline val Double.quart: Double get() = this * this * this * this
inline val Float.quart: Float get() = this * this * this * this
inline val Int.quart: Int get() = this * this * this * this

inline val Double.quint: Double get() = this * this * this * this * this
inline val Float.quint: Float get() = this * this * this * this * this
inline val Int.quint: Int get() = this * this * this * this * this