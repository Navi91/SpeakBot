package com.dkrasnov.speakbot.di

import android.app.Application
import android.content.Context
import com.dkrasnov.speakbot.preferences.Preferences
import com.dkrasnov.speakbot.token.TokenProvider
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun providePreferences(context: Context) : Preferences{
        return Preferences(context)
    }

    @Provides
    @Singleton
    fun provideTokenProvider(context: Context, preferences: Preferences) : TokenProvider {
        return TokenProvider(context, preferences)
    }
}