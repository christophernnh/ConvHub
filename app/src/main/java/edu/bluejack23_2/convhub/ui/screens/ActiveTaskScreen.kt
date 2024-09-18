package edu.bluejack23_2.convhub.ui.theme.screens


import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.bluejack23_2.convhub.di.RepositoryModule
import edu.bluejack23_2.convhub.ui.CreateTaskActivity
import edu.bluejack23_2.convhub.ui.screens.detailLister.DetailListerScreen
import edu.bluejack23_2.convhub.ui.screens.jobdetail.JobDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ActiveTaskScreen(
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val jobs by viewModel.userJobsState.collectAsState()

    val jobRepository = RepositoryModule.provideJobRepository()

    LaunchedEffect(Unit) {
        viewModel.fetchUserJobs()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "My Posted Jobs",
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Button(
                    onClick = {
                        val intent = Intent(context, CreateTaskActivity::class.java)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = MaterialTheme.colors.onPrimary
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Create a job")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(jobs) { job ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            backgroundColor = Color.LightGray
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable {
                                        val intent =
                                            Intent(context, DetailListerScreen::class.java).apply {
                                                putExtra("jobId", job.id)
                                            }
                                        context.startActivity(intent)
                                    }

                            ) {
                                Text(text = job.title, fontSize = 20.sp, color = Color.Blue)
                                Text(text = "Price: $${job.price}", color = Color(0xFF6699CC))
                                Text(
                                    text = "Requirements: ${job.categories.joinToString(", ")}",
                                    color = Color(0xFF6699CC)
                                )
                                Button(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            jobRepository.removeJobByID(job.id)
                                            viewModel.fetchUserJobs()
                                        }
                                    }
                                ) {
                                    Text(text = "Remove Job")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
