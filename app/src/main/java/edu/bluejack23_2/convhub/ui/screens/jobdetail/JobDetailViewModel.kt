package edu.bluejack23_2.convhub.ui.screens.jobdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bluejack23_2.convhub.data.model.Job
import edu.bluejack23_2.convhub.data.model.User
import edu.bluejack23_2.convhub.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userJobsState = MutableStateFlow<List<Job>>(emptyList())
    val userJobsState = _userJobsState.asStateFlow()

    fun fetchUserJobs() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val documents = firestore.collection("job")
                    .whereEqualTo("jobLister", userId)
                    .get()
                    .await()
                val jobs = documents.mapNotNull { document ->
                    val job = document.toObject(Job::class.java)
                    if (job != null) {
                        val username = userRepository.fetchUsernameByUid(job.jobLister)
                        job.copy(id = document.id, jobLister = username ?: job.jobLister)
                    } else {
                        null
                    }
                }
                jobs
                _userJobsState.value = jobs
            } catch (e: Exception) {
                Log.e("JobDetailViewModel", "Error fetching user jobs", e)
                _userJobsState.value = emptyList()
            }
        }
    }

    private val _jobState = MutableStateFlow<Job?>(null)
    val jobState = _jobState.asStateFlow()

    private val _posterProfileState = MutableStateFlow<User?>(null)
    val posterProfileState = _posterProfileState.asStateFlow()

    fun fetchJobDetails() {
        viewModelScope.launch {
            try {
                val document = firestore.collection("job").document("jehoCTfD3EBWJ5YIYj78").get().await()
                val job = document.toObject(Job::class.java)
                _jobState.value = job

                // Fetch poster profile if job details are successfully fetched
                job?.jobLister?.let { fetchPosterProfile(it) }
            } catch (e: Exception) {
                Log.e("JobDetailViewModel", "Error fetching job details", e)
                _jobState.value = null
            }
        }
    }

    private fun fetchPosterProfile(posterID: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(posterID).get().await()
                val posterProfile = document.toObject(User::class.java)
                _posterProfileState.value = posterProfile
            } catch (e: Exception) {
                Log.e("JobDetailViewModel", "Error fetching poster profile", e)
                _posterProfileState.value = null
            }
        }
    }
}




