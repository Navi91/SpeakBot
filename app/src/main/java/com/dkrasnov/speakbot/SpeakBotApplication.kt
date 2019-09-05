package com.dkrasnov.speakbot

import android.app.Application
import com.dkrasnov.speakbot.di.ComponentHolder

class SpeakBotApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ComponentHolder.init(this)
    }
}