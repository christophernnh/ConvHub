package edu.bluejack23_2.convhub.ui.screens.jobdetail

import android.os.Bundle
import android.provider.ContactsContract.Profile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack23_2.convhub.ui.screens.profile.ProfileScreen
import edu.bluejack23_2.convhub.ui.theme.ConvHubTheme
import edu.bluejack23_2.convhub.ui.viewmodel.ProfileViewModel

@AndroidEntryPoint
class JobActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConvHubTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    JobDetailScreen()
                }
            }
        }
    }
}
@Composable
fun JobDetailScreen(
    viewModel: JobDetailViewModel = hiltViewModel(),
) {
    val job by viewModel.jobState.collectAsState()
    val posterProfile by viewModel.posterProfileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchJobDetails()
    }

    ConvHubTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Job Details",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            when (job) {
                null -> Text(text = "Loading...")
                else -> {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        items(items = job!!.imageUris) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(text = job?.title ?: "", fontSize = 30.sp)
                            Text(text = job?.address ?: "", color = Color(0xFF6699CC))
                            Text(text = "one-time")
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(text = "Rp.${job?.price}", fontSize = 30.sp)
                            Text("Payout", color = Color(0xFF6699CC))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(text = "Details:", fontSize = 20.sp)
                        Text(text = job?.description ?: "", color = Color(0xFF6699CC))
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Requirements:", fontSize = 20.sp)
                        Text(text = job?.categories?.joinToString(", ") ?: "", color = Color(0xFF6699CC))
                    }

                    Button(
                        onClick = {
                            // Handle apply for job click
                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Blue,
                            contentColor = MaterialTheme.colors.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(text = "Apply for job")
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "posted by:", color = Color(0xFF6699CC))
                    }

                    posterProfile?.let { profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            backgroundColor = Color.LightGray
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(profile.picture),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = profile.username, fontSize = 20.sp, color = Color.Blue)
                                    Text(text = profile.email, color = Color(0xFF6699CC))
                                    Text(text = "More details >",fontSize = 15.sp,color = Color.Gray)
                                }
                            }
                        }
                    } ?: Text(text = "Loading poster details...")
                    Text(text = "Posted at: ${job?.posted_at}",fontSize = 15.sp,color = Color.Gray)
                }
            }
        }
    }
}



