package com.dkrasnov.speakbot.speach_api.di

import com.dkrasnov.speakbot.speach_api.BotSpeakResponse
import com.dkrasnov.speakbot.speach_api.QueryInput
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SpeakApi {

    @POST("projects/{project-id}/agent/sessions/{session-id}:detectIntent")
    fun doPostDetectIntent(
        @Path("project-id") projectId: String,
        @Path("session-id") sessionId: String,
        @Body queryInput: QueryInput
    ): Single<BotSpeakResponse>
}