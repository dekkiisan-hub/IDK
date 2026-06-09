package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waifu_characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val personality: String, // e.g. "Tsundere", "Kuudere", "Cheerful / Dandere", "Custom"
    val systemPrompt: String,
    val relationshipType: String, // e.g. "Schoolmate", "Mentor", "Childhood Friend", "Girlfriend"
    val avatarResName: String, // e.g. "img_anime_kaguya", "img_anime_yuki", "img_anime_sakura"
    val affection: Int = 10,  // ranges 0 to 100
    val intimacy: Int = 1,    // levels 1 to 10
    val mood: String = "Normal", // "Blushing", "Shy", "Flustered", "Cheerful", "Cool"
    val customThemeColor: String = "Purple" // "Purple", "Pink", "Cyan", "Indigo"
)
