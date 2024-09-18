package edu.bluejack23_2.convhub.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.bluejack23_2.convhub.data.model.Applicant
import edu.bluejack23_2.convhub.data.model.Job
import edu.bluejack23_2.convhub.data.model.User
import edu.bluejack23_2.convhub.di.RepositoryModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class JobRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userRepository = RepositoryModule.provideUserRepository()
    private var jobListener: ListenerRegistration? = null
    private val storage = FirebaseStorage.getInstance()

    suspend fun fetchJobs(): List<Job> {
        return try {
            val jobDocuments = db.collection("job").get().await().documents
            val jobs = jobDocuments.mapNotNull { document ->
                val job = document.toObject(Job::class.java)
                if (job != null) {
                    val username = userRepository.fetchUsernameByUid(job.jobLister)
                    job.copy(jobLister = username ?: "" , id = document.id )

                } else {
                    null
                }
            }
            jobs
        } catch (e: Exception) {
            Log.e("JobRepository", "Error fetching jobs", e)
            emptyList()
        }
    }


    suspend fun fetchJobsByPreferredFields(preferredFields: List<String>): List<Job> {
        val allJobs = fetchJobs()
        return allJobs.filter { job ->
            preferredFields.isEmpty() || job.categories.any { it in preferredFields }
        }
    }

    suspend fun removeJobByID(jobId: String) {
        try {
            db.collection("job").document(jobId)
                .delete()
                .await()
            Log.d("JobRepository", "DocumentSnapshot successfully deleted!")
        } catch (e: Exception) {
            Log.e("JobRepository", "Error deleting document", e)
        }
    }

    suspend fun fetchJobWithApplicants(jobId: String): Pair<Job?, List<User>> {
        val jobRef = db.collection("job").document(jobId)
        val jobSnapshot = jobRef.get().await()
        val job = jobSnapshot.toObject(Job::class.java)
        val users = job?.let {
            fetchApplicants(it.applicants.map { applicant -> applicant.userId })
        } ?: emptyList()
        return Pair(job, users)
    }

    suspend fun getJobById(jobId : String) : Job? {
        return try {
            val jobRef = db.collection("job").document(jobId)
            val snapshot = jobRef.get().await()
            val job = snapshot.toObject(Job::class.java)
            job?.copy(id = snapshot.id)

        } catch (e: Exception) {
            Log.e("JobRepository", "Error fetching jobs", e)
            null
        }
    }

    private suspend fun fetchApplicants(userIds: List<String>): List<User> {
        val users = mutableListOf<User>()
        for (userId in userIds) {
            val userRef = db.collection("users").document(userId)
            val userSnapshot = userRef.get().await()
            userSnapshot.toObject(User::class.java)?.let {
                val user = it.copy(id = userId)
                users.add(user)
            }

        }
        return users
    }

    suspend fun applyJobByUserId(jobId : String, userId : String) {
        try {
            val jobRef = db.collection("job").document(jobId)
            val jobSnapshot = jobRef.get().await()
            val job = jobSnapshot.toObject(Job::class.java)

            if (job != null) {
                val applicants = job.applicants.toMutableList()

                applicants.add(Applicant(userId = userId, status = "pending"))

                jobRef.update("applicants", applicants).await()
                Log.d("JobRepository", "User successfully applied to job!")
            } else {
                Log.e("JobRepository", "Job not found")
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Error applying to job", e)
        }
    }

    suspend fun unApplyJobByUserId(jobId : String, userId : String) {
        try {
            val jobRef = db.collection("job").document(jobId)
            val jobSnapshot = jobRef.get().await()
            val job = jobSnapshot.toObject(Job::class.java)

            if (job != null) {
                val applicants = job.applicants.toMutableList()
                val applicantIndex = applicants.indexOfFirst { it.userId == userId }
                if (applicantIndex != -1) {
                    applicants.removeAt(applicantIndex)
                    jobRef.update("applicants", applicants).await()
                    Log.d("JobRepository", "User successfully unapplied to job!")
                } else {
                    Log.e("JobRepository", "Applicant not found")
                }
            } else {
                Log.e("JobRepository", "Job not found")
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Error unapplying to job", e)
        }
    }

    fun addJobApplicantsListener(
        jobId: String,
        scope: CoroutineScope,
        onApplicantsChanged: (List<User>) -> Unit
    ) {
        jobListener = db.collection("job").document(jobId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("JobRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val job = snapshot.toObject(Job::class.java)
                    job?.let {
                        scope.launch {
                            val users = fetchApplicants(it.applicants.map { applicant -> applicant.userId })
                            onApplicantsChanged(users)
                        }
                    }
                } else {
                    Log.d("JobRepository", "Current data: null")
                }
            }
    }

    suspend fun getJobsByDateAndUser(date: Date, userId: String): List<Job> {
        val jobsCollection = db.collection("job")
        val startOfDay = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return try {
            val querySnapshot = jobsCollection
                .whereEqualTo("jobTaker", userId)
                .whereGreaterThanOrEqualTo("taken_at", startOfDay)
                .whereLessThanOrEqualTo("taken_at", endOfDay)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Job::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun finishJob(jobId: String, paymentProofUri: Uri) {
        try {
            val storageRef: StorageReference = storage.reference.child("payment_proof/$jobId.jpg")
            val uploadTask = storageRef.putFile(paymentProofUri).await()
            val downloadUrl = uploadTask.metadata?.reference?.downloadUrl?.await()

            val jobRef = db.collection("job").document(jobId)
            val jobSnapshot = jobRef.get().await()
            val job = jobSnapshot.toObject(Job::class.java)

            if (job != null && downloadUrl != null) {
                val updatedJob = job.copy(status = "finished", paymentProofUrl = downloadUrl.toString())
                jobRef.set(updatedJob).await()
                Log.d("JobRepository", "Job successfully finished!")
            } else {
                Log.e("JobRepository", "Job not found or error in uploading image")
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Error finishing job", e)
        }
    }

    suspend fun getTakenJobs(userId: String): List<Job> {
        val jobsCollection = db.collection("job")

        return try {
            val querySnapshot = jobsCollection
                .whereEqualTo("jobTaker", userId)
                .whereIn("status", listOf("taken", "completed"))
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Job::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAvailableJobs(userId: String): List<Job> {
        val jobsCollection = db.collection("job")

        return try {
            val querySnapshot = jobsCollection
                .whereEqualTo("jobLister", userId)
                .whereIn("status", listOf("untaken"))
                .get()
                .await()
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Job::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPreviousJobs(userId: String): List<Job> {
        val jobsCollection = db.collection("job")

        return try {
            val querySnapshot = jobsCollection
                .whereEqualTo("jobLister", userId)
                .whereIn("status", listOf("taken", "completed"))
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Job::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun removeJobApplicantsListener() {
        jobListener?.remove()
    }

    suspend fun acceptJobApplicant(jobId: String, userId: String) {
        try {
            val jobRef = db.collection("job").document(jobId)
            val jobSnapshot = jobRef.get().await()
            val job = jobSnapshot.toObject(Job::class.java)

            if (job != null) {
                val applicants = job.applicants.toMutableList()
                val applicantIndex = applicants.indexOfFirst { it.userId == userId }
                if (applicantIndex != -1) {
                    applicants[applicantIndex] = applicants[applicantIndex].copy(status = "accepted")
                    val updatedJob = job.copy(applicants = applicants, jobTaker = userId)
                    jobRef.set(updatedJob).await()
                    Log.d("JobRepository", "User successfully accepted as job taker!")
                } else {
                    Log.e("JobRepository", "Applicant not found")
                }
            } else {
                Log.e("JobRepository", "Job not found")
            }
        } catch (e: Exception) {
            Log.e("JobRepository", "Error accepting job applicant", e)
        }
    }

}
