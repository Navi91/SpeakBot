package com.dkrasnov.speakbot.chat.di

import com.dkrasnov.speakbot.chat.domain.GetBotMessageUseCase
import com.dkrasnov.speakbot.preferences.Preferences
import com.dkrasnov.speakbot.speach_api.di.SpeakApi
import com.dkrasnov.speakbot.token.TokenProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ChatModule {

    @Singleton
    @Provides
    fun provideGetBotMessageUseCase(
        tokenProvider: TokenProvider,
        speakApi: SpeakApi,
        preferences: Preferences
    ): GetBotMessageUseCase {
        return GetBotMessageUseCase(tokenProvider, speakApi, preferences)
    }
}