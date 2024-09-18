package edu.bluejack23_2.convhub.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.bluejack23_2.convhub.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun fetchCurrentUser() : User? {
        val currentUser = auth.currentUser
        return currentUser?.let { user ->
            val documentSnapshot = db.collection("users")
                .document(user.uid)
                .get()
                .await()
            val newUser = documentSnapshot.toObject(User::class.java)
            newUser?.copy(id = user.uid)
        }

    }

    suspend fun fetchUsernameByUid(uid: String): String? {
        return try {
            val documentSnapshot = db.collection("users").document(uid).get().await()
            documentSnapshot.toObject(User::class.java)?.username
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching username", e)
            null
        }
    }

    suspend fun fetchUserByUid(uid: String): User? {
        return try {
            val documentSnapshot = db.collection("users").document(uid).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            user?.copy(id = uid)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user", e)
            null
        }
    }


    suspend fun savePreferredFields(fields: List<String>) {
        val fields = hashMapOf(
            "preferredFields" to fields
        )
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).set(fields, SetOptions.merge())
                .addOnSuccessListener { Log.d("SUCCESS", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("ERROR", "Error writing document", e) }
        }
    }
}