package com.example.data

import kotlinx.coroutines.flow.Flow

class WaifuRepository(private val dao: WaifuDao) {
    val allCharacters: Flow<List<CharacterEntity>> = dao.getAllCharacters()

    suspend fun getCharacterById(id: Int): CharacterEntity? = dao.getCharacterById(id)

    suspend fun insertCharacter(character: CharacterEntity): Long = dao.insertCharacter(character)

    suspend fun updateCharacter(character: CharacterEntity) = dao.updateCharacter(character)

    suspend fun deleteCharacter(id: Int) = dao.deleteCharacter(id)

    fun getMessagesForCharacter(charId: Int): Flow<List<MessageEntity>> = dao.getMessagesForCharacter(charId)

    suspend fun insertMessage(message: MessageEntity) = dao.insertMessage(message)

    suspend fun clearMessagesForCharacter(charId: Int) = dao.clearMessagesForCharacter(charId)
}
