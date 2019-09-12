package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class QueryResult(@SerializedName("fulfillmentText") val message: String, val fulfillmentMessages: Array<FulfillMessage>) {

    fun getFullFillMessage(): String {
        return fulfillmentMessages.joinToString("\n") { it.getFirstMessage() }
    }
}