package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ActiveScreen
import com.example.ui.WaifuViewModel
import com.example.ui.screens.CharacterCustomizerScreen
import com.example.ui.screens.WaifuChatScreen
import com.example.ui.screens.WaifuGalleryScreen
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core TTS engine initialize
        try {
            tts = TextToSpeech(this, this)
        } catch (e: Exception) {
            Log.e("TTSMatrix", "Failed to initialize TTS: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WaifuViewModel = viewModel()

                    // Register speak callback so ViewModel can request voice readout
                    viewModel.registerSpeakCallback { speechText ->
                        speakOut(speechText)
                    }

                    val screen by viewModel.activeScreen.collectAsState()
                    val characters by viewModel.characters.collectAsState()
                    val activeChar by viewModel.activeCharacter.collectAsState()
                    val messages by viewModel.messages.collectAsState()
                    val isSending by viewModel.isSending.collectAsState()

                    // Crossfade animation transitions between Screens
                    Crossfade(
                        targetState = screen,
                        animationSpec = tween(durationMillis = 300),
                        label = "ScreenTransitions"
                    ) { activeState ->
                        when (activeState) {
                            ActiveScreen.Gallery -> {
                                WaifuGalleryScreen(
                                    viewModel = viewModel,
                                    characters = characters
                                )
                            }
                            ActiveScreen.Chat -> {
                                WaifuChatScreen(
                                    viewModel = viewModel,
                                    character = activeChar,
                                    messages = messages,
                                    isSending = isSending
                                )
                            }
                            ActiveScreen.Customizer -> {
                                CharacterCustomizerScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSMatrix", "TTS English Language is not supported on this device.")
            } else {
                isTtsInitialized = true
                // Setup Voice Pitch and Speed rate (higher pitch for sweet anime voice tone!)
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(0.95f)
            }
        } else {
            Log.e("TTSMatrix", "Initialization of TextToSpeech failed.")
        }
    }

    private fun speakOut(text: String) {
        if (!isTtsInitialized || tts == null) return
        try {
            // Remove emojis and interactive actions from text so TTS reading sounds standard
            val cleanSpeech = text
                .replace(Regex("\\(\\*.*?\\*\\)"), "") // removes interactive indicators like (*Interactive interaction*)
                .replace(Regex("[^a-zA-Z0-9\\s.,!?']"), "") // removes emojis and symbols
                .trim()

            if (cleanSpeech.isNotEmpty()) {
                tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "WaifuAudioOutput")
            }
        } catch (e: Exception) {
            Log.e("TTSMatrix", "Error in speaking: ${e.message}")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
