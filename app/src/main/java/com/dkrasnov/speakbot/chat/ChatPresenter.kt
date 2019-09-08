package com.dkrasnov.speakbot.chat

import android.media.MediaRecorder
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.preferences.Preferences
import java.io.IOException
import javax.inject.Inject

@InjectViewState
class ChatPresenter : MvpPresenter<IChatView>() {

    @Inject
    lateinit var preferences: Preferences

    private var recorder: MediaRecorder? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        ComponentHolder.applicationComponent().inject(this)
    }

    override fun detachView(view: IChatView?) {
        super.detachView(view)

        onStopRecord()
    }

    fun onStartRecord() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(MessageHelper.getAudioFileNameFromIndex(preferences.getAndIncrementUserMessageInde()))
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                log("prepare() failed")
            }

            start()
        }
    }

    fun onStopRecord() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }
}