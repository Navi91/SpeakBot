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
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.transition.TransitionManager
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.dkrasnov.speakbot.R
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.speach_api.di.SpeakApi
import com.dkrasnov.speakbot.token.TokenProvider
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.a_main.*
import java.util.*
import javax.inject.Inject


class ChatActivity : MvpAppCompatActivity(), IChatView, RecognitionListener,
    TextToSpeech.OnInitListener {

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
    private lateinit var textToSpeech: TextToSpeech
    private val params = hashMapOf<String, String>().apply {
        put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak_bot_utterance_id")
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        ComponentHolder.applicationComponent().inject(this)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        textToSpeech = TextToSpeech(this, this, "com.google.android.tts")

        textToSpeech.setOnUtteranceCompletedListener {
            log("on text to speech completed $it")
            runOnUiThread {
                startRecord()
            }
        }

        recordButton.setOnClickListener {
            log("request start record")

            if (checkPermission()) {
                startRecord()
            } else {
                requestPermissions()
            }
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
        textToSpeech.shutdown()
    }

    private fun startRecord() {
        log("start record")

        isRecordRunning = true

        recordButton.isEnabled = false
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

        recordButton.isEnabled = true
        scaleDownRecordButton()
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

        val speakMessage = message.replace("KFC", "кэй эф си")

        textToSpeech.speak(speakMessage, TextToSpeech.QUEUE_ADD, params)
    }

    override fun onInit(status: Int) {
        log("on text to speech init status: $status")

        if (status == TextToSpeech.SUCCESS) {
            val setLanguageResult = textToSpeech.setLanguage(Locale.forLanguageTag("ru"))

            if (setLanguageResult == TextToSpeech.LANG_MISSING_DATA || setLanguageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                log("Language not supported")
                showError("Russian not supported")
            } else {
                log("Language install success")
            }
        } else {
            log("Text to speech install failed")
            showError("Text to speech install failed")
        }
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

        stopRecord()
    }

    override fun onError(error: Int) {
        log("on error $error")

        stopRecord()
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
            RxPermissions(this).request(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                .subscribe({
                    log("request permissions result: $it")
                }, {
                    showError(it.message)
                })
    }
}
