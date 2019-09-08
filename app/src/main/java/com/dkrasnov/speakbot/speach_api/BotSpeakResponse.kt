package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class BotSpeakResponse(@SerializedName("queryResult") val queryResult: QueryResult) {

    val message
        get() = queryResult.message

}