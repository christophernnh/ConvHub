package edu.bluejack23_2.convhub.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bluejack23_2.convhub.data.model.User
import edu.bluejack23_2.convhub.di.FirebaseModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.*
import android.util.Patterns
import edu.bluejack23_2.convhub.data.repository.UserRepository
import edu.bluejack23_2.convhub.di.RepositoryModule_ProvideUserRepositoryFactory
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> get() = _userState

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            userId?.let {
                val document = firestore.collection("users").document(it).get().await()
                val user = document.toObject(User::class.java)
                user?.let { _userState.value = it }
            }
        }
    }

    fun isAdult(dob: Date?): Boolean {
        if (dob == null) return false
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.time = dob
        val birthYear = calendar.get(Calendar.YEAR)
        return (currentYear - birthYear) >= 18
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun evaluatePasswordStrength(password: String): String {
        var strengthPoints = 0

        // Check if password is at least 8 characters long
        if (password.length >= 8) strengthPoints++

        // Check if password has alphabetic characters
        if (password.any { it.isLetter() }) strengthPoints++

        // Check if password has numeric characters
        if (password.any { it.isDigit() }) strengthPoints++

        // Check if password has special characters
        val specialCharacters = "!@#\$%^&*()-_=+[]{}|;:'\",<.>/?"
        if (password.any { specialCharacters.contains(it) }) strengthPoints++

        return when (strengthPoints) {
            1 -> "Weak"
            2 -> "Medium"
            3 -> "Strong"
            4 -> "Very Strong"
            else -> "Very Weak"
        }
    }

    fun changePassword(password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val strength = evaluatePasswordStrength(password)
        if (strength != "Strong" && strength != "Very Strong") {
            onFailure("Password must be at least Strong")
            return
        }

        auth.currentUser?.let { user ->
            user.updatePassword(password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess("Password changed successfully")
                    } else {
                        onFailure("Failed to change password: ${task.exception?.message}")
                    }
                }
        }
    }

    fun updateUserProfile(
        username: String,
        email: String,
        dob: Date?,
        picture: String,
        jobs: List<String>,
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                when {
                    !isValidEmail(email) -> {
                        onFailure("Please enter a valid email address")
                    }
                    !isAdult(dob) -> {
                        onFailure("You must be at least 18 years old")
                    }
                    imageUri != null -> {
                        uploadImage(imageUri, onSuccess = { imageUrl ->
                            val user = User(dob ?: Date(), username, userId, email, imageUrl, preferredFields =  jobs)
                            firestore.collection("users").document(userId).set(user)
                            _userState.value = user
                            onSuccess("Profile updated successfully")
                        }, onFailure = { exception ->
                            onFailure("Error updating profile: ${exception.message}")
                        })
                    }
                    else -> {
                        val user = User(dob ?: Date(), username, userId, email, picture, preferredFields = jobs)
                        firestore.collection("users").document(userId).set(user).await()
                        _userState.value = user
                        onSuccess("Profile updated successfully")
                    }
                }
            }
        }
    }

    private fun uploadImage(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageReference = storage.reference.child("profile_images/${auth.currentUser?.uid}.jpg")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                    _userState.value = _userState.value.copy(picture = uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val user = userRepository.fetchUserByUid(userId)
            user?.let {
                _userState.value = it
            }
        }
    }

    fun refreshProfile() {
        fetchUserProfile()
    }
}
