package com.dkrasnov.speakbot.chat

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.dkrasnov.speakbot.R
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.preferences.Preferences
import javax.inject.Inject

@InjectViewState
class ChatPresenter : MvpPresenter<IChatView>() {

    @Inject
    lateinit var context: Context
    @Inject
    lateinit var preferences: Preferences

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        ComponentHolder.applicationComponent().inject(this)
    }

    fun onVoiceMessage(message: String?) {
        if (message.isNullOrEmpty()) {
            viewState.showError(context.getString(R.string.error_not_recognized_message))
            return
        }

        viewState.setUserMessage(message)
    }
}