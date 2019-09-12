package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class FulfillMessage(@SerializedName("text") val messageText: FulfillMessageText) {

    fun getFirstMessage() = messageText.text.firstOrNull() ?: ""
}