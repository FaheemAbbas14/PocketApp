package com.faheem.pocketapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UtilityEntry(
    val id: String,
    val title: String,
    val note: String,
    val updatedAt: Long
)

data class UtilityUiState(
    val isLoading: Boolean = false,
    val currentUserEmail: String? = null,
    val entries: List<UtilityEntry> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    var uiState by mutableStateOf(UtilityUiState())
        private set

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var entriesListener: ListenerRegistration? = null

    init {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            handleUserChanged(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authListener!!)
        handleUserChanged(auth.currentUser)
    }

    fun login(email: String, password: String) {
        if (!validateCredentials(email, password)) return

        executeAuthAction {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
        }
    }

    fun register(email: String, password: String) {
        if (!validateCredentials(email, password)) return

        executeAuthAction {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun addEntry(title: String, note: String) {
        val user = auth.currentUser ?: run {
            setError("Please log in first.")
            return
        }
        val trimmedTitle = title.trim()
        val trimmedNote = note.trim()
        if (trimmedTitle.isBlank() && trimmedNote.isBlank()) {
            setError("Add a title or note before saving.")
            return
        }

        viewModelScope.launch {
            try {
                val document = entriesCollection(user.uid).document()
                val payload = mapOf(
                    "title" to trimmedTitle,
                    "note" to trimmedNote,
                    "updatedAt" to System.currentTimeMillis()
                )
                document.set(payload).await()
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to save entry.")
            }
        }
    }

    fun deleteEntry(entryId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                entriesCollection(user.uid).document(entryId).delete().await()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to delete entry.")
            }
        }
    }

    private fun executeAuthAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                action()
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Authentication failed.")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun handleUserChanged(user: FirebaseUser?) {
        entriesListener?.remove()
        entriesListener = null

        if (user == null) {
            uiState = UtilityUiState()
            return
        }

        uiState = uiState.copy(
            currentUserEmail = user.email,
            isLoading = true,
            errorMessage = null
        )

        entriesListener = entriesCollection(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    setError(error.message ?: "Unable to sync backup data.")
                    uiState = uiState.copy(isLoading = false)
                    return@addSnapshotListener
                }

                val mappedEntries = snapshot?.documents
                    ?.map { document ->
                        UtilityEntry(
                            id = document.id,
                            title = document.getString("title").orEmpty(),
                            note = document.getString("note").orEmpty(),
                            updatedAt = document.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()

                uiState = uiState.copy(entries = mappedEntries, isLoading = false)
            }
    }

    private fun entriesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("entries")

    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            setError("Email and password are required.")
            return false
        }
        if (password.length < 6) {
            setError("Password must be at least 6 characters.")
            return false
        }
        return true
    }

    private fun setError(message: String) {
        uiState = uiState.copy(errorMessage = message)
    }

    private fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    override fun onCleared() {
        authListener?.let(auth::removeAuthStateListener)
        entriesListener?.remove()
        super.onCleared()
    }
}
