package com.dkrasnov.speakbot.chat

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.dkrasnov.speakbot.R
import com.dkrasnov.speakbot.chat.domain.GetBotMessageUseCase
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.preferences.Preferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@InjectViewState
class ChatPresenter : MvpPresenter<IChatView>() {

    @Inject
    lateinit var context: Context
    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var getBotMessageUseCase: GetBotMessageUseCase

    private var disposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        ComponentHolder.applicationComponent().inject(this)

        preferences.setSessionId(UUID.randomUUID().toString())
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()
    }

    fun onVoiceMessage(message: String?) {
        if (message.isNullOrEmpty()) {
            viewState.showError(context.getString(R.string.error_not_recognized_message))
            return
        }

        viewState.setProcessingServerResponseState()
        viewState.setUserMessage(message)

        disposable =  getBotMessageUseCase.getBotMessage(message)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                log("take bot message $it")

                viewState.setBotMessage(it)
            }, {
                log("bot message error: $it")
            })
    }
}