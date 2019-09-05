package com.dkrasnov.speakbot.extensions

import android.util.Log

fun Any.log(message: String?) {
    Log.d(this.javaClass.simpleName, message)
}

fun Any.log(error: Throwable?) {
    Log.e(this.javaClass.simpleName, error?.message)
}