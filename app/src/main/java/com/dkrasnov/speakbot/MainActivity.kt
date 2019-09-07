package com.dkrasnov.speakbot

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dkrasnov.speakbot.di.ComponentHolder
import com.dkrasnov.speakbot.extensions.log
import com.dkrasnov.speakbot.speach_api.QueryInput
import com.dkrasnov.speakbot.speach_api.di.SpeakApi
import com.dkrasnov.speakbot.token.TokenProvider
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var speakApi: SpeakApi
    @Inject
    lateinit var tokenProvider: TokenProvider

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}
