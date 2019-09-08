package com.dkrasnov.speakbot.chat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.transition.TransitionManager
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.dkrasnov.speakbot.R
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.speach_api.QueryInput
import com.dkrasnov.speakbot.speach_api.di.SpeakApi
import com.dkrasnov.speakbot.token.TokenProvider
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.a_main.*
import javax.inject.Inject


class ChatActivity : MvpAppCompatActivity(), IChatView, RecognitionListener {

    companion object {
        private const val SCALE_UP_VALUE = 1.2f
        private const val SCALE_DOWN_VALUE = 1f
    }

    @InjectPresenter
    lateinit var presenter: ChatPresenter

    @Inject
    lateinit var speakApi: SpeakApi
    @Inject
    lateinit var tokenProvider: TokenProvider

    private var recordButtonAnimator: Animator? = null
    private var permissionDisposable: Disposable? = null
    private var isRecordRunning = false
    private lateinit var speechRecognizer: SpeechRecognizer

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        val queryInput = QueryInput.create("Привет")

        ComponentHolder.applicationComponent().inject(this)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)


//        tokenProvider.loadToken()
//            .doOnSuccess {
//                log("token: $it")
//            }
//            .flatMap {
//                speakApi.doPostDetectIntent("kfc-demo-iybbyn", "120", queryInput)
//            }
//            .subscribeOn(Schedulers.io())
//            .subscribe({
//                log(it.toString())
//            }, {
//                log(it)
//            })

        recordButton.setOnTouchListener { _, event ->
            when {
                event.action == MotionEvent.ACTION_DOWN -> {
                    log("request start record")

                    if (checkPermission()) {
                        startRecord()
                    } else {
                        requestPermissions()
                    }
                }
                event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL -> {
                    log("request cancel record")

                    if (isRecordRunning) {
                        stopRecord()
                    }
                }
            }

            return@setOnTouchListener false
        }
    }

    override fun onStop() {
        super.onStop()

        permissionDisposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()

        speechRecognizer.run {
            stopListening()
            destroy()
        }
    }

    private fun startRecord() {
        log("start record")

        isRecordRunning = true


        setRecordState()
        scaleUpRecordButton()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_CALLING_PACKAGE,
            "com.dkrasnov.speakbot"
        )

        speechRecognizer.setRecognitionListener(this)
        speechRecognizer.startListening(intent)
    }

    private fun stopRecord() {
        log("stop record")

        isRecordRunning = false

        scaleDownRecordButton()

        speechRecognizer.cancel()
    }

    override fun setRecordState() {
        setStateText(R.string.say)
    }

    override fun setProcessingUserRecordState() {
        setStateText(R.string.processing_message)
    }

    override fun setSendRequestToServerState() {
        setStateText(R.string.send_request_to_server)
    }

    override fun setProcessingServerResponseState() {
        setStateText(R.string.processing_server_response)
    }

    override fun setUserMessage(message: String) {
        userMessageTextView.text = message
    }

    override fun setBotMessage(message: String) {
        botMessageTextView.text = message
    }

    override fun showError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        log("ready for speech $params")
    }

    override fun onRmsChanged(rmsdB: Float) {
//        log("on rms changed $rmsdB")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        log("on buffer received")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        log("on partial results $partialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        log("on event $eventType $params")
    }

    override fun onBeginningOfSpeech() {
        log("on beginning of speech")
    }

    override fun onEndOfSpeech() {
        log("on end of speech")

//        stopRecord()
    }

    override fun onError(error: Int) {
        log("on error $error")

//        stopRecord()
    }

    override fun onResults(results: Bundle?) {
        val voices = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

        log("on results $voices")

        presenter.onVoiceMessage(voices?.firstOrNull())
    }

    private fun scaleUpRecordButton() {
        scaleRecordButton(SCALE_UP_VALUE)
    }

    private fun scaleDownRecordButton() {
        scaleRecordButton(SCALE_DOWN_VALUE)
    }

    private fun scaleRecordButton(scale: Float) {
        recordButtonAnimator?.cancel()

        recordButtonAnimator = AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(recordButton, View.SCALE_X, scale))
                .with(ObjectAnimator.ofFloat(recordButton, View.SCALE_Y, scale))
        }
        recordButtonAnimator?.start()
    }

    private fun setStateText(@StringRes textRes: Int) {
        TransitionManager.beginDelayedTransition(contentLayout)

        stateTextView.setText(textRes)
    }

    private fun checkPermission(): Boolean {
        val result = RxPermissions(this).isGranted(Manifest.permission.RECORD_AUDIO)
                && RxPermissions(this).isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        log("check permission result: $result")

        return result
    }

    private fun requestPermissions() {
        permissionDisposable =
            RxPermissions(this).request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe({
                    log("request permissions result: $it")
                }, {
                    showError(it.message)
                })
    }
}
