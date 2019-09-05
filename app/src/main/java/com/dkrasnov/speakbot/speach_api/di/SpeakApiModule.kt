package com.dkrasnov.speakbot.speach_api.di

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class SpeakApiModule {

    @Provides
    fun provideSpeakApi(retrofit: Retrofit) : SpeakApi {
        return retrofit.create(SpeakApi::class.java)
    }
}