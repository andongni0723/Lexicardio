package com.andongni.vcblearn.ui.component

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.util.Locale

@HiltViewModel
class TtsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel(), TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ready = tts.setLanguage(Locale.US) >= 0
        }
    }

    fun speak(text: String) {
        if (!ready) {
            Toast.makeText(context, "play word sound failed.", Toast.LENGTH_SHORT).show()
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}
