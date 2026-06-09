package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import com.example.api.Content as ApiContent
import com.example.api.Part as ApiPart
import com.example.data.AppDatabase
import com.example.data.CharacterEntity
import com.example.data.MessageEntity
import com.example.data.WaifuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ActiveScreen {
    Gallery,
    Chat,
    Customizer
}

class WaifuViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = WaifuRepository(database.waifuDao())

    // UI Screen navigation
    private val _activeScreen = MutableStateFlow(ActiveScreen.Gallery)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Loaded characters
    val characters: StateFlow<List<CharacterEntity>> = repository.allCharacters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Highlighted selected character
    private val _selectedCharId = MutableStateFlow<Int?>(null)
    val selectedCharId: StateFlow<Int?> = _selectedCharId.asStateFlow()

    private val _activeCharacter = MutableStateFlow<CharacterEntity?>(null)
    val activeCharacter: StateFlow<CharacterEntity?> = _activeCharacter.asStateFlow()

    // Reactive messages list for chosen character
    val messages: StateFlow<List<MessageEntity>> = _selectedCharId.flatMapLatest { id ->
        if (id != null) {
            repository.getMessagesForCharacter(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat sending indicator
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // TTS speak event callback link for Compose UI
    private var speakEventCallback: ((String) -> Unit)? = null

    // Forms inside Customizer Screen
    val customName = MutableStateFlow("")
    val customPersonality = MutableStateFlow("Tsundere")
    val customRelationType = MutableStateFlow("Companion")
    val customBackstory = MutableStateFlow("")
    val customAvatarChoice = MutableStateFlow("img_anime_kaguya")
    val customThemeColor = MutableStateFlow("Purple")

    fun registerSpeakCallback(callback: (String) -> Unit) {
        speakEventCallback = callback
    }

    fun navigateTo(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun selectCharacter(charId: Int) {
        _selectedCharId.value = charId
        viewModelScope.launch {
            val char = repository.getCharacterById(charId)
            _activeCharacter.value = char
            _activeScreen.value = ActiveScreen.Chat
        }
    }

    fun backToGallery() {
        _selectedCharId.value = null
        _activeCharacter.value = null
        _activeScreen.value = ActiveScreen.Gallery
    }

    // Triggered on active physical interaction (tapping on avatar face/body)
    fun interactWithWaifu() {
        val char = _activeCharacter.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val newAffection = (char.affection + 2).coerceAtMost(100)
            val newIntimacy = if (newAffection >= 90) 5 else if (newAffection >= 70) 4 else if (newAffection >= 50) 3 else if (newAffection >= 30) 2 else 1

            // Dynamic dialogue according to character preset types
            val touchResponse = when (char.personality) {
                "Tsundere" -> {
                    val texts = listOf(
                        "D-Don't touch me so suddenly, baka! Y-you startled me...",
                        "W-What do you think you're doing?! It's not like I like your touch!",
                        "Fine, you can pat my head, but ONLY for a few seconds! Got it?!"
                    )
                    texts.random()
                }
                "Kuudere" -> {
                    val texts = listOf(
                        "I am observing an anomalous increase in my core temperature. Explain.",
                        "Your touch is comfortable. I do not dislike this interaction.",
                        "Affection metric has updated positively. Silence remains stable."
                    )
                    texts.random()
                }
                else -> { // Cheerful
                    val texts = listOf(
                        "Yayy! I love headpats! Do that even more, please!",
                        "Hehehe! That tickles! Today is the absolute best day ever!",
                        "Mou! You're making my face turn as pink as my hair! So happy!"
                    )
                    texts.random()
                }
            }

            val updatedChar = char.copy(
                affection = newAffection,
                intimacy = newIntimacy,
                mood = "Blushing (*´∀｀*)"
            )
            repository.updateCharacter(updatedChar)
            _activeCharacter.value = updatedChar

            // Create system feedback message or TTS readout
            withContext(Dispatchers.Main) {
                speakEventCallback?.invoke(touchResponse)
            }

            // Insert conversational log
            val systemMsg = MessageEntity(
                characterId = char.id,
                isUser = false,
                text = "(*Interactive interaction*) $touchResponse"
            )
            repository.insertMessage(systemMsg)
        }
    }

    // Nurtures affection instantly
    fun declareLove() {
        val char = _activeCharacter.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val newAffection = (char.affection + 6).coerceAtMost(100)
            val newIntimacy = if (newAffection >= 90) 6 else if (newAffection >= 70) 4 else if (newAffection >= 50) 3 else 2
            val updatedChar = char.copy(affection = newAffection, intimacy = newIntimacy, mood = "Affectionate (◌•ω•◌)")
            repository.updateCharacter(updatedChar)
            _activeCharacter.value = updatedChar

            val userMsg = MessageEntity(
                characterId = char.id,
                isUser = true,
                text = "I love you. You are my perfect soul companion."
            )
            repository.insertMessage(userMsg)

            _isSending.value = true

            // Trigger actual Gemini API conversational response based on love confession!
            val contextMsgList = messages.value.takeLast(10).map {
                ApiContent(
                    parts = listOf(ApiPart(text = it.text)),
                    role = if (it.isUser) "user" else "model"
                )
            }.toMutableList()
            contextMsgList.add(ApiContent(parts = listOf(ApiPart(text = userMsg.text)), role = "user"))

            val apiResponse = GeminiApiClient.chatWithWaifu(contextMsgList, updatedChar.systemPrompt)

            repository.insertMessage(
                MessageEntity(
                    characterId = char.id,
                    isUser = false,
                    text = apiResponse
                )
            )
            _isSending.value = false

            withContext(Dispatchers.Main) {
                speakEventCallback?.invoke(apiResponse)
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val char = _activeCharacter.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _isSending.value = true

            // 1. Insert user message to database
            val userMsg = MessageEntity(
                characterId = char.id,
                isUser = true,
                text = text
            )
            repository.insertMessage(userMsg)

            // 2. Fetch history of conversations (last 10 messages)
            val history = messages.value.takeLast(10).map {
                ApiContent(
                    parts = listOf(ApiPart(text = it.text)),
                    role = if (it.isUser) "user" else "model"
                )
            }.toMutableList()
            history.add(ApiContent(parts = listOf(ApiPart(text = text)), role = "user"))

            // 3. Call AI REST backend
            val modelResponse = GeminiApiClient.chatWithWaifu(history, char.systemPrompt)

            // Random small increase in affection/stats just by chatting
            val randVal = (1..10).random()
            var nextAffection = char.affection
            var nextMood = char.mood
            if (randVal > 7) {
                nextAffection = (char.affection + 1).coerceAtMost(100)
                nextMood = "Friendly (*^ω^*)"
            }

            val updatedChar = char.copy(
                affection = nextAffection,
                mood = nextMood
            )
            repository.updateCharacter(updatedChar)
            _activeCharacter.value = updatedChar

            // 4. Save model response
            val modelMsg = MessageEntity(
                characterId = char.id,
                isUser = false,
                text = modelResponse
            )
            repository.insertMessage(modelMsg)
            _isSending.value = false

            // Speak response dynamically
            withContext(Dispatchers.Main) {
                speakEventCallback?.invoke(modelResponse)
            }
        }
    }

    fun clearChat() {
        val charId = _selectedCharId.value ?: return
        viewModelScope.launch {
            repository.clearMessagesForCharacter(charId)
        }
    }

    fun deleteCharacter(charId: Int) {
        viewModelScope.launch {
            repository.deleteCharacter(charId)
            repository.clearMessagesForCharacter(charId)
            if (_selectedCharId.value == charId) {
                backToGallery()
            }
        }
    }

    fun createCharacter() {
        val name = customName.value.trim()
        val personality = customPersonality.value
        val relation = customRelationType.value
        val promptBackstory = customBackstory.value.trim()

        if (name.isEmpty()) return

        val finalSystemPrompt = "You are $name, a cute anime girl, playing the role of $relation. Your personality is $personality. Here is your backstory: $promptBackstory. Keep responses short (under 3 sentences), conversational, expressive and stay strictly in character!"

        val newChar = CharacterEntity(
            name = name,
            personality = personality,
            systemPrompt = finalSystemPrompt,
            relationshipType = relation,
            avatarResName = customAvatarChoice.value,
            affection = 10,
            intimacy = 1,
            mood = "Shy ( ´∀｀)",
            customThemeColor = customThemeColor.value
        )

        viewModelScope.launch(Dispatchers.IO) {
            val generatedId = repository.insertCharacter(newChar).toInt()

            // Pre-add a warm first greeting message to start the romance journey!
            val welcomeText = when (personality) {
                "Tsundere" -> "Ugh... So you're the one who summoned me? It's not like I'm happy to meet you or anything, baka! But since we're here, make sure you treat me well!"
                "Kuudere" -> "Connection initialized. System protocol defines our relationship as $relation. Understood. I will wait for your command."
                else -> "Wow! Hello hello! I am so excited to finally meet you! Let's make so many amazing memories together starting today!"
            }

            repository.insertMessage(
                MessageEntity(
                    characterId = generatedId,
                    isUser = false,
                    text = welcomeText
                )
            )

            withContext(Dispatchers.Main) {
                backToGallery()
                // Clear forms
                customName.value = ""
                customBackstory.value = ""
            }
        }
    }
}
