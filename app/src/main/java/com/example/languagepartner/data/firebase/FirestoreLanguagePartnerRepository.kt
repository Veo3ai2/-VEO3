package com.example.languagepartner.data.firebase

import android.content.Context
import android.util.Log
import com.example.languagepartner.R
import com.example.languagepartner.data.model.Feedback
import com.example.languagepartner.data.model.Message
import com.example.languagepartner.data.model.UserProgress
import com.example.languagepartner.data.model.VocabWord
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreLanguagePartnerRepository(context: Context) {

    private val databaseId = context.getString(R.string.firestore_database_id)
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProgress(userId: String): Flow<UserProgress> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, "users/$userId")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val progress = UserProgress(
                    streak = (data["streak"] as? Number)?.toInt() ?: 0,
                    totalMessagesSent = (data["totalMessagesSent"] as? Number)?.toInt() ?: 0,
                    grammarAccuracyScore = (data["grammarAccuracyScore"] as? Number)?.toInt() ?: 100,
                    lastActiveDate = data["lastActiveDate"] as? String,
                    dailyMessageGoal = (data["dailyMessageGoal"] as? Number)?.toInt() ?: 10,
                    todayMessagesSent = (data["todayMessagesSent"] as? Number)?.toInt() ?: 0,
                    dailyVocabGoal = (data["dailyVocabGoal"] as? Number)?.toInt() ?: 5,
                    todayVocabAdded = (data["todayVocabAdded"] as? Number)?.toInt() ?: 0
                )
                trySend(progress)
            } else {
                trySend(UserProgress())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProgress(userId: String, progress: UserProgress) {
        val uid = requireUserId()
        if (uid != userId) {
            throw SecurityException("Unauthorized UID mismatch")
        }
        val path = "users/$userId"
        try {
            val payload = mapOf(
                "userId" to uid,
                "streak" to progress.streak,
                "totalMessagesSent" to progress.totalMessagesSent,
                "grammarAccuracyScore" to progress.grammarAccuracyScore,
                "dailyMessageGoal" to progress.dailyMessageGoal,
                "todayMessagesSent" to progress.todayMessagesSent,
                "dailyVocabGoal" to progress.dailyVocabGoal,
                "todayVocabAdded" to progress.todayVocabAdded,
                "lastActiveDate" to progress.lastActiveDate,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeVocabWords(userId: String): Flow<List<VocabWord>> = callbackFlow {
        val colRef = db.collection("users").document(userId).collection("vocab")
        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "users/$userId/vocab")
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                VocabWord(
                    id = doc.id,
                    word = data["word"] as? String ?: "",
                    translation = data["translation"] as? String ?: "",
                    languageName = data["languageName"] as? String ?: "",
                    contextSentence = data["context"] as? String ?: "",
                    notes = data["notes"] as? String,
                    timestamp = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                        ?: System.currentTimeMillis()
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addVocabWord(
        userId: String,
        id: String,
        word: String,
        translation: String,
        languageName: String,
        context: String,
        notes: String?
    ) {
        val uid = requireUserId()
        if (uid != userId) throw SecurityException("Unauthorized UID mismatch")
        val path = "users/$userId/vocab/$id"
        try {
            val payload = mapOf(
                "id" to id,
                "userId" to uid,
                "word" to word.trim(),
                "translation" to translation.trim(),
                "languageName" to languageName,
                "context" to context,
                "notes" to notes,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).collection("vocab").document(id).set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun removeVocabWord(userId: String, id: String) {
        val uid = requireUserId()
        if (uid != userId) throw SecurityException("Unauthorized UID mismatch")
        val path = "users/$userId/vocab/$id"
        try {
            db.collection("users").document(userId).collection("vocab").document(id).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    fun observeChatMessages(userId: String, partnerId: String): Flow<List<Message>> = callbackFlow {
        val colRef = db.collection("users").document(userId).collection("chat_messages")
        val listener = colRef
            .whereEqualTo("partnerId", partnerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/chat_messages")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val roleStr = data["role"] as? String ?: "user"
                    val isUser = roleStr == "user"
                    val grammarCorrect = data["grammarCorrect"] as? Boolean
                    val corrected = data["correctedText"] as? String
                    val explanation = data["explanation"] as? String

                    val feedback = if (!isUser && (!grammarCorrect!! || corrected != null || explanation != null)) {
                        Feedback(
                            isGrammaticallyCorrect = grammarCorrect ?: true,
                            correctedSentence = corrected,
                            explanation = explanation,
                            vocabSuggestions = emptyList()
                        )
                    } else null

                    val ts = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                        ?: System.currentTimeMillis()

                    Message(
                        id = doc.id,
                        text = data["text"] as? String ?: "",
                        isFromUser = isUser,
                        timestamp = ts,
                        feedback = feedback
                    )
                }?.sortedBy { it.timestamp } ?: emptyList()

                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveChatMessage(userId: String, partnerId: String, message: Message) {
        val uid = requireUserId()
        if (uid != userId) throw SecurityException("Unauthorized UID mismatch")
        val path = "users/$userId/chat_messages/${message.id}"
        try {
            val payload = mapOf(
                "id" to message.id,
                "userId" to uid,
                "partnerId" to partnerId,
                "role" to if (message.isFromUser) "user" else "partner",
                "text" to message.text,
                "grammarCorrect" to message.feedback?.isGrammaticallyCorrect,
                "correctedText" to message.feedback?.correctedSentence,
                "explanation" to message.feedback?.explanation,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).collection("chat_messages").document(message.id).set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun clearChatMessages(userId: String, partnerId: String) {
        val uid = requireUserId()
        if (uid != userId) throw SecurityException("Unauthorized UID mismatch")
        val path = "users/$userId/chat_messages"
        try {
            val snapshot = db.collection("users").document(userId).collection("chat_messages")
                .whereEqualTo("partnerId", partnerId)
                .get()
                .await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }
}
