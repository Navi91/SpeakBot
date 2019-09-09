package com.dkrasnov.speakbot.chat.domain

import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.preferences.Preferences
import com.dkrasnov.speakbot.speach_api.QueryInput
import com.dkrasnov.speakbot.speach_api.di.SpeakApi
import com.dkrasnov.speakbot.token.TokenProvider
import io.reactivex.Single
import retrofit2.HttpException

class GetBotMessageUseCase(
    private val tokenProvider: TokenProvider,
    private val speakApi: SpeakApi,
    private val preferences: Preferences
) {

    fun getBotMessage(userMessage: String): Single<String> {

        return getBotMessageWithTokenProvider(userMessage, tokenProvider.loadToken()).onErrorResumeNext {
            log("get bot message error: ${it.message}")

            if (it is HttpException && it.code() == 401) {
                log("is auth exception")

                return@onErrorResumeNext getBotMessageWithTokenProvider(userMessage, tokenProvider.rerfeshToken())
            }

            Single.error(it)
        }

    }

    private fun getBotMessageWithTokenProvider(userMessage: String, tokenProvider: Single<String>) : Single<String> {
        return tokenProvider.flatMap {
            val projectId = "kfc-demo-iybbyn"
            val queryInput = QueryInput.create(userMessage)
            val sessionId = preferences.getSessionId()

            log("get bot response for project: $projectId query: $queryInput session: $sessionId")

            speakApi.doPostDetectIntent(projectId, sessionId, queryInput)
        }.map {
            log("server response $it")

            it.message
        }
    }
}