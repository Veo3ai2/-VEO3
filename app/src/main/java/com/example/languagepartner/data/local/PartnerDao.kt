package com.example.languagepartner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocab_words ORDER BY createdAt DESC")
    fun getAllVocabWords(): Flow<List<VocabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabWord(word: VocabEntity)

    @Query("DELETE FROM vocab_words WHERE id = :id")
    suspend fun deleteVocabWord(id: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE partnerId = :partnerId ORDER BY timestamp ASC")
    fun getMessagesForPartner(partnerId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE partnerId = :partnerId")
    suspend fun clearMessagesForPartner(partnerId: String)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getProgress(): Flow<ProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ProgressEntity)
}
