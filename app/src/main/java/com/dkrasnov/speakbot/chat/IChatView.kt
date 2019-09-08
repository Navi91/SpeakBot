package com.dkrasnov.speakbot.chat

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface IChatView : MvpView {

    fun setRecordState()

    fun setProcessingUserRecordState()

    fun setSendRequestToServerState()

    fun setProcessingServerResponseState()

    fun setUserMessage(message: String)

    fun setBotMessage(message: String)

    @StateStrategyType(SkipStrategy::class)
    fun showError(message: String?)
}