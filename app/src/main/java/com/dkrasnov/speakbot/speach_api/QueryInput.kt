package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class QueryInput(@SerializedName("query_input") val data: SpeechTextData) {

    companion object {

        fun create(message: String): QueryInput {
            return QueryInput(SpeechTextData(SpeechText(message)))
        }
    }
}