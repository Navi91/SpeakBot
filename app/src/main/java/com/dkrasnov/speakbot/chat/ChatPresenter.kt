package com.dkrasnov.speakbot.chat

import android.content.Context
import android.media.MediaRecorder
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.extensions.createAudoMessageFilePath
import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.preferences.Preferences
import java.io.IOException
import javax.inject.Inject

@InjectViewState
class ChatPresenter : MvpPresenter<IChatView>() {

    @Inject
    lateinit var context: Context
    @Inject
    lateinit var preferences: Preferences

    private var recorder: MediaRecorder? = null
    private var audioFilePath: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        ComponentHolder.applicationComponent().inject(this)
    }

    override fun detachView(view: IChatView?) {
        super.detachView(view)

        onStopRecord()
    }

    fun onStartRecord() {
        log("on start record")

        audioFilePath = context.createAudoMessageFilePath(preferences.getAndIncrementUserMessageInde())

        log("audio file path: $audioFilePath")

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFilePath)
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
        log("on stop record")

        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }
}