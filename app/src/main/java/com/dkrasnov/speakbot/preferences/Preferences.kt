package com.dkrasnov.speakbot.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class Preferences(private val context: Context) {

    companion object {

        private const val SPEAK_BOT_PREFS = "speak_bot_prefs"

        private const val USER_MESSAGE_INDEX_PREF_KEY = "user_message_index_pref_key"
        private const val SESSION_ID_PREF_KEY = "session_id_pref_key"
        private const val LAST_USER_MESSAGE_TIME_PREF_KEY = "last_user_message_time_pref_key"
    }

    private val sharedPreferences =
        context.getSharedPreferences(SPEAK_BOT_PREFS, Context.MODE_PRIVATE)

    fun getAndIncrementUserMessageInde(): Int {
        val index = getUserMessageIndex()
        incrementUserMessageIndex()
        return index
    }

    private fun incrementUserMessageIndex() {
        syncEdit {
            putInt(USER_MESSAGE_INDEX_PREF_KEY, getUserMessageIndex() + 1)
        }
    }

    private fun getUserMessageIndex(): Int {
        return sharedPreferences.getInt(USER_MESSAGE_INDEX_PREF_KEY, 0)
    }

    fun getSessionId(): String {
        return sharedPreferences.getString(SESSION_ID_PREF_KEY, "") ?: ""
    }

    fun setSessionId(id: String) {
        syncEdit {
            putString(SESSION_ID_PREF_KEY, id)
        }
    }

    fun getLastUserMessageTime(): Long {
        return sharedPreferences.getLong(LAST_USER_MESSAGE_TIME_PREF_KEY, 0L)
    }

    fun setLastUserMessageTime(time: Long) {
        syncEdit {
            putLong(LAST_USER_MESSAGE_TIME_PREF_KEY, time)
        }
    }

    @SuppressLint("ApplySharedPref")
    fun syncEdit(edit: SharedPreferences.Editor.() -> Unit) {
        val editor = sharedPreferences.edit()
        edit.invoke(editor)
        editor.commit()
    }

    fun getString(key: String, defaultValue: String = ""): String? =
        sharedPreferences.getString(key, defaultValue)
}