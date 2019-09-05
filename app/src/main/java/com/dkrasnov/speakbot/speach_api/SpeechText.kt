package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class SpeechText(val text: String, @SerializedName("language_code") val languageCode: String = "ru")