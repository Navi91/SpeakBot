package com.dkrasnov.speakbot.chat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
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
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.a_main.*
import javax.inject.Inject

class ChatActivity : MvpAppCompatActivity(), IChatView {

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

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        val queryInput = QueryInput.create("Привет")

        ComponentHolder.applicationComponent().inject(this)

        tokenProvider.loadToken()
            .doOnSuccess {
                log("token: $it")
            }
            .flatMap {
                speakApi.doPostDetectIntent("kfc-demo-iybbyn", "120", queryInput)
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                log(it.toString())
            }, {
                log(it)
            })

        recordButton.setOnTouchListener { _, event ->
            when {
                event.action == MotionEvent.ACTION_DOWN -> {
                    log("request start record")

                    if (checkPermission()) {
                        scaleUpRecordButton()
                        presenter.onStartRecord()
                    } else {
                        requestPermissions()
                    }
                }
                event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL -> {
                    log("cancel record")

                    scaleDownRecordButton()
                    presenter.onStopRecord()
                }
            }

            return@setOnTouchListener false
        }
    }

    override fun onStop() {
        super.onStop()

        permissionDisposable?.dispose()
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
