package com.dkrasnov.speakbot.di

import android.app.Application
import com.dkrasnov.speakbot.MainActivity

object ComponentHolder {

    private lateinit var applicationComponent: AppComponent

    fun init(application: Application) {
        applicationComponent = DaggerAppComponent.builder().setApplication(application).build()
    }

    fun applicationComponent() = applicationComponent
}