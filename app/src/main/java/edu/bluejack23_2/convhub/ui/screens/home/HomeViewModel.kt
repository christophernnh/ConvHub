package edu.bluejack23_2.convhub.ui.screens.home

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bluejack23_2.convhub.data.model.Job
import edu.bluejack23_2.convhub.data.model.User
import edu.bluejack23_2.convhub.data.repository.JobRepository
import edu.bluejack23_2.convhub.data.repository.UserRepository
import edu.bluejack23_2.convhub.ui.events.UiEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository
) : ViewModel() {


    private val _jobState = MutableStateFlow<List<Job>>(emptyList())
    val jobState = _jobState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery = _searchQuery.asStateFlow()

    private val _preferredFields = MutableStateFlow<List<String>>(emptyList())
    val preferredFields = _preferredFields.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _jobPreferred = MutableStateFlow<List<Job>>(emptyList())
    val jobPreferred = _jobPreferred.asStateFlow()

    init {
        fetchJobs()
        fetchPreferredFields()
        getCurrentUser()
    }

    fun fetchJobs() {
        viewModelScope.launch {
            try {
                val jobs = jobRepository.fetchJobs()
                _jobState.value = jobs
            } catch (e: Exception) {
                Log.e("JobRepository", "Error fetching jobs", e)
            }
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.fetchCurrentUser()
                _user.value = user
            } catch (e: Exception) {
                Log.e("JobRepository", "Error fetching jobs", e)
            }
        }
    }

    private fun fetchPreferredJobs(preferredFields : List<String>) {
        viewModelScope.launch {
            try {
                val jobs = jobRepository.fetchJobsByPreferredFields(preferredFields)
                _jobPreferred.value = jobs
            } catch (e: Exception) {
                Log.e("JobRepository", "Error fetching jobs", e)
            }
        }
    }

    private fun fetchPreferredFields() {
        viewModelScope.launch {
            try {
                val fields = userRepository.fetchCurrentUser()?.preferredFields

                if (fields != null) {
                    _preferredFields.value = fields
                    fetchPreferredJobs(fields)
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching preferred fields", e)
            }
        }
    }

    fun updateSearchQuery(query: TextFieldValue) {
        viewModelScope.launch {
            _searchQuery.emit(query)
        }
    }

    fun savePreferredFields(fields: List<String>) {
        viewModelScope.launch {
            try {
                userRepository.savePreferredFields(fields)
                _preferredFields.value = fields
                _uiEvent.emit(UiEvent.ShowToast("Preferences saved successfully"))

                fetchJobs()
                fetchPreferredFields()
                fetchPreferredJobs(fields)

            } catch (e: Exception) {
                Log.e("JobRepository", "Error saving preferred fields", e)
                _uiEvent.emit(UiEvent.ShowToast("Error saving preferences"))
            }
        }
    }
}
