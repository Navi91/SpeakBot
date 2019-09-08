package com.dkrasnov.speakbot.speach_api

import com.google.gson.annotations.SerializedName

data class QueryResult(@SerializedName("fulfillmentText") val message: String)