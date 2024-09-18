package edu.bluejack23_2.convhub.ui.screens.jobtakerprofile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack23_2.convhub.R
import edu.bluejack23_2.convhub.ui.theme.ConvHubTheme
import edu.bluejack23_2.convhub.ui.theme.screens.JobCard
import edu.bluejack23_2.convhub.ui.viewmodel.ProfileViewModel

@AndroidEntryPoint
class JobTakerProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("userId") ?: return
        setContent {
            ConvHubTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    JobTakerProfileScreen(userId)
                }
            }
        }
    }
}


@Composable
fun JobTakerProfileScreen(
    userId: String,
    viewModel: ProfileViewModel = hiltViewModel(),
    JobTakerProfileViewModel: JobTakerProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val jobs by JobTakerProfileViewModel.jobs.collectAsState()


    LaunchedEffect(userState) {
        viewModel.loadUser(userId)
        Log.d("this is the uid:", userState.username)
        JobTakerProfileViewModel.fetchTakenJobs(userId)
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable {
                        (context as ComponentActivity).finish()
                    }
                    .align(Alignment.TopStart)
            )
        }

        Text(
            text = "Task Taker Details",
            fontSize = 20.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        if (userState.picture.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.convhub_logo_only_white),
                contentDescription = "Placeholder Image",
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = userState.picture),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userState.username,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "User Rating Average: 3.0 Stars",
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color.Gray, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(text = "Quality of Service", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "3.0 Stars", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color.Gray, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(text = "Punctuality", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "3.0 Stars", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color.Gray, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(text = "Attitude", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "3.0 Stars", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Previous Jobs",
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
        ) {
            if (jobs.isEmpty()) {
                item {
                    Text(
                        text = "No Jobs",
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                items(jobs) { job ->
                    JobCard(job)
                }
            }
        }

//        Button(
//            onClick = {
//                val intent = Intent(context, CreateTaskActivity::class.java)
//                context.startActivity(intent)
//            },
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = Color.Blue,
//                contentColor = MaterialTheme.colors.onPrimary
//            ),
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text("Create Job")
//        }
    }
}