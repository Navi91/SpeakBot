package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class SpeechTextData(@SerializedName("text") val speechText: SpeechText)