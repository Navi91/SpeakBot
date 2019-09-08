package com.dkrasnov.speakbot.chat

class MessageHelper {

    companion object {

        fun getAudioFileNameFromIndex(index: Int): String {
            return "user_audio_message_$index"
        }


    }
}