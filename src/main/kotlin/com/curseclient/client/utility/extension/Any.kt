package com.curseclient.client.utility.extension

fun <T: Any> T.transformIf(flag: Boolean, block: (prev: T) -> T) =
    if (flag) block(this) else this