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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class ChatPresenter : MvpPresenter<IChatView>() {

    companion object {
        private val SESSION_INTERVAL = TimeUnit.DAYS.toMillis(5)
    }

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

        updateSessionId()
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

        updateSessionIdIfNeed()
        preferences.setLastUserMessageTime(System.currentTimeMillis())

        viewState.setProcessingServerResponseState()
        viewState.setUserMessage(message)

        disposable = getBotMessageUseCase.getBotMessage(message)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                log("take bot message $it")

                viewState.setBotMessage(it)
            }, {
                log("bot message error: $it")
            })
    }

    private fun updateSessionIdIfNeed() {
        log("update session id if need current time: ${System.currentTimeMillis()} last message time: ${preferences.getLastUserMessageTime()}")

        if (System.currentTimeMillis() - preferences.getLastUserMessageTime() > SESSION_INTERVAL) {
            updateSessionId()
        }
    }

    private fun updateSessionId() {
        val sessionId = UUID.randomUUID().toString()

        log("update session id: $sessionId")

        preferences.setSessionId(sessionId)
    }
}