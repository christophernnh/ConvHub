package edu.bluejack23_2.convhub.ui.screens.jobtakerprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bluejack23_2.convhub.data.model.Job
import edu.bluejack23_2.convhub.data.repository.JobRepository
import edu.bluejack23_2.convhub.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JobTakerProfileViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs

    private val _previousjobs = MutableStateFlow<List<Job>>(emptyList())
    val previousjobs: StateFlow<List<Job>> = _previousjobs

    private val _availablejobs = MutableStateFlow<List<Job>>(emptyList())
    val availablejobs: StateFlow<List<Job>> = _availablejobs

    fun fetchTakenJobs(userId: String) {
        viewModelScope.launch {
            _jobs.value = jobRepository.getTakenJobs(userId)
        }
    }

    fun fetchAvailableJobs(userId: String){
      viewModelScope.launch {
          _availablejobs.value = jobRepository.getAvailableJobs(userId)
      }
    }

    fun fetchPreviousJobs(userId: String){
        viewModelScope.launch {
            _previousjobs.value = jobRepository.getPreviousJobs(userId)
        }
    }
}