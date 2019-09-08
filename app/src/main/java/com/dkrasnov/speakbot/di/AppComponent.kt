package com.dkrasnov.speakbot.di

import android.app.Application
import com.dkrasnov.speakbot.chat.ChatActivity
import com.dkrasnov.speakbot.chat.ChatPresenter
import com.dkrasnov.speakbot.speach_api.di.SpeakApiModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, RetrofitModule::class, SpeakApiModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun setApplication(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(chatActivity: ChatActivity)
    fun inject(chatActivity: ChatPresenter)

}