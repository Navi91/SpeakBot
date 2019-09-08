package com.dkrasnov.speakbot.token

import android.content.Context
import com.dkrasnov.speakbot.R
import com.dkrasnov.speakbot.preferences.Preferences
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import io.reactivex.Single

class TokenProvider(private val context: Context, private val preferences: Preferences) {

    companion object {

        private const val TOKEN_PREF_KEY = "token_pref_key"
    }

    fun loadToken(): Single<String> {
        val savedToken = preferences.getString(TOKEN_PREF_KEY)

        if (!savedToken.isNullOrEmpty()) {
            return Single.just(savedToken)
        }

        return rerfeshToken()
    }

    fun rerfeshToken() : Single<String> {
        return Single.fromCallable {
            var accessToken = ""
            context.resources.openRawResource(R.raw.credential).use { stream ->
                val credential = GoogleCredential.fromStream(stream)
                    .createScoped(listOf("https://www.googleapis.com/auth/dialogflow"))
                credential.refreshToken()
                accessToken = credential.accessToken
            }

            if (accessToken.isEmpty()) {
                throw GetAccessTokenException()
            }

            accessToken
        }.doOnSuccess {
            preferences.syncEdit {
                putString(TOKEN_PREF_KEY, it)
            }
        }
    }

    fun getToken() : String {
        return preferences.getString(TOKEN_PREF_KEY) ?: ""
    }
}