package com.dkrasnov.speakbot.extensions

import android.content.Context
import java.io.File

fun getAudioFileNameFromIndex(index: Int): String {
    return "user_audio_message_$index.aac"
}

fun Context.createAudoMessageFilePath(index: Int): String {
    return File(this.externalCacheDir, getAudioFileNameFromIndex(index)).path
}