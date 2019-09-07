package com.dkrasnov.speakbot.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class Preferences(private val context: Context) {

    companion object {

        private const val SPEAK_BOT_PREFS = "speak_bot_prefs"

        private const val ACCESS_TOKEN_PREF_KEY = "access_token_pref_key"
    }

    private val sharedPreferences = context.getSharedPreferences(SPEAK_BOT_PREFS, Context.MODE_PRIVATE)

    @SuppressLint("ApplySharedPref")
    fun syncEdit(edit: SharedPreferences.Editor.() -> Unit) {
        val editor = sharedPreferences.edit()
        edit.invoke(editor)
        editor.commit()
    }

    fun getString(key: String, defaultValue: String = "") : String? = sharedPreferences.getString(key, defaultValue)
}