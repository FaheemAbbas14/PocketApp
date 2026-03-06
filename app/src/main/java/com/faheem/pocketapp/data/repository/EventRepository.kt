package com.faheem.pocketapp.data.repository

import com.faheem.pocketapp.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

interface EventRepository {
    suspend fun addEvent(title: String, description: String, eventDateMillis: Long, alarmEnabled: Boolean): Result<Unit>
    suspend fun updateEvent(item: EventItem): Result<Unit>
    suspend fun deleteEvent(item: EventItem): Result<Unit>
    fun observeEvents(userId: String): kotlinx.coroutines.flow.Flow<List<EventItem>>
}

class EventRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : EventRepository {

    override suspend fun addEvent(title: String, description: String, eventDateMillis: Long, alarmEnabled: Boolean): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val document = eventsCollection(user.uid).document()
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "title" to title.trim(),
                "description" to description.trim(),
                "eventDateMillis" to eventDateMillis,
                "alarmEnabled" to alarmEnabled,
                "updatedAt" to now
            )
            document.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEvent(item: EventItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val payload = mapOf(
                "title" to item.title.trim(),
                "description" to item.description.trim(),
                "eventDateMillis" to item.eventDateMillis,
                "alarmEnabled" to item.alarmEnabled,
                "updatedAt" to System.currentTimeMillis()
            )
            eventsCollection(user.uid).document(item.id).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(item: EventItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            eventsCollection(user.uid).document(item.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeEvents(userId: String): kotlinx.coroutines.flow.Flow<List<EventItem>> {
        return kotlinx.coroutines.flow.callbackFlow {
            val listener = eventsCollection(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents
                    ?.map { doc ->
                        EventItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            eventDateMillis = doc.getLong("eventDateMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.eventDateMillis }
                    ?: emptyList()
                trySend(events)
            }
            awaitClose { listener.remove() }
        }
    }

    private fun eventsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("events")
}

