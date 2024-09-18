package edu.bluejack23_2.convhub.ui.screens.detailLister

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack23_2.convhub.R
import edu.bluejack23_2.convhub.data.model.Job
import edu.bluejack23_2.convhub.data.model.User
import edu.bluejack23_2.convhub.ui.events.UiEvent
import edu.bluejack23_2.convhub.ui.screens.joblisterprofile.JobListerProfileActivity
import edu.bluejack23_2.convhub.ui.screens.jobtakerprofile.JobTakerProfileActivity
import edu.bluejack23_2.convhub.ui.theme.ConvHubTheme
import edu.bluejack23_2.convhub.ui.theme.DarkBlue
import kotlinx.coroutines.flow.collectLatest
import java.util.Date

@AndroidEntryPoint
class DetailListerScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jobId = intent.getStringExtra("jobId") ?: "jehoCTfD3EBWJ5YIYj78"
        setContent {
            ConvHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    JobDetailScreen(jobId)
                }
            }
        }
    }
}

@Composable
fun JobDetailScreen(jobId: String, viewModel: DetailListerViewModel = hiltViewModel()) {
    val jobDetail by viewModel.jobDetail.collectAsState()
    val applicants by viewModel.applicants.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(jobId) {
        viewModel.loadJobDetail(jobId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ConvHubTheme {
        LazyColumn {
            item {
                jobDetail?.let { job ->
                    val updatedJob = job.copy(id = jobId)
                    JobDetailContent(
                        updatedJob,
                        applicants,
                        viewModel
                    )
                } ?: Text(text = "Loading...")
            }
        }
    }
}

@Composable
fun JobDetailContent(job: Job, applicants: List<User>, viewModel: DetailListerViewModel? = null) {

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(DarkBlue)
        ) {
            Image(
                painter = rememberImagePainter(data = job.imageUris.firstOrNull()),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable {
                        (context as ComponentActivity).finish()
                    }
                    .align(Alignment.TopStart)
            )
        }
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = job.title,
                fontSize = 24.sp,
                fontWeight = FontWeight(600)
            )
            Text(text = job.address, fontWeight = FontWeight(500), color = Color.Gray)
            Text(
                text = job.categories.joinToString(separator = " • ") { it },
                color = Color.Gray,
                fontWeight = FontWeight(500)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = job.description,
                    color = Color.Gray,
                    fontWeight = FontWeight(500)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Posted by ",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight(500)
                    )
                    ClickableText(
                        text = AnnotatedString(job.jobListerUsername),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = DarkBlue,
                            fontWeight = FontWeight(500)
                        ),
                        onClick = {
                            val intent =
                                Intent(context, JobListerProfileActivity::class.java).apply {
                                    putExtra("userId", job.jobLister)
                                }
                            context.startActivity(intent)
                        }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_star),
                        contentDescription = "Star",
                        tint = Color.Yellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = job.rating.toString())
                }
            }
            Text(
                text = "Applicants",
                fontSize = 22.sp,
                fontWeight = FontWeight(600)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (job.jobTaker.isNotEmpty()) {
                val acceptedApplicant = applicants.find { it.id == job.jobTaker }

                if (acceptedApplicant != null) {
                    ApplicationCard(
                        applicant = acceptedApplicant,
                        onClick = {},
                        isAccepted = true
                    ) {
                        val intent = Intent(context, JobTakerProfileActivity::class.java).apply {
                            putExtra("userId", it.id)
                        }
                        context.startActivity(intent)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 14.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                if (job.status != "finished") showDialog = true;
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (job.status == "finished") Color.LightGray else DarkBlue
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (job.status == "finished") "Job is Finished" else "Finish Job",
                                color = Color.White,
                                fontWeight = FontWeight(500),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                applicants.forEach { applicant ->
                    ApplicationCard(applicant, onClick = {
                        viewModel?.acceptJobRequest(job.id, applicant.id)
                    }) {
                        val intent = Intent(context, JobTakerProfileActivity::class.java).apply {
                            putExtra("userId", it.id)
                        }
                        context.startActivity(intent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

        }

        if (showDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(5f)
            ) {
                FinishJobDialog(
                    onDismiss = { showDialog = false },
                    onFinish = { uri ->
                        viewModel?.finishJobRequest(job.id, uri)
                        showDialog = false
                    }
                )
            }
        }

    }
}

@Composable
fun FinishJobDialog(onDismiss: () -> Unit, onFinish: (Uri) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.background,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Upload Payment Proof", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        launcher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DarkBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Select Image")
                }

                imageUri?.let {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            imageUri?.let { onFinish(it) }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = DarkBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Finish", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun ApplicationCard(
    applicant: User,
    isAccepted: Boolean = false,
    onClick: () -> Unit,
    onClickApplicant: (User) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isAccepted) Color.Green else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .clickable { onClickApplicant(applicant) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = applicant.username, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = applicant.email, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (!isAccepted) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    backgroundColor = DarkBlue
                ),
                shape = CircleShape,
                modifier = Modifier.size(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check Icon",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun JobDetailScreenPreview() {
    val mockJob = Job(
        id = "1",
        address = "123 Street",
        categories = listOf("Category1", "Category2"),
        imageUris = listOf("https://example.com/image1.jpg"),
        jobTaker = "JobTaker1",
        jobLister = "JobLister1",
        price = 100,
        status = "Open",
        title = "Mock Job Title",
        rating = 4.5f,
        description = "This is a mock job description. The job involves several detailed tasks that require a high level of skill and attention to detail. You will be responsible for managing multiple aspects of the project, including but not limited to task coordination, resource management, and ensuring timely delivery of all project components. Additionally, strong communication skills are necessary as you will be collaborating with various team members and stakeholders to ensure that all project requirements are met. This position offers a great opportunity for professional growth and development in a dynamic and fast-paced environment.",
        posted_at = java.util.Date()
    )

    val dummyUsers = listOf(
        User(
            dob = Date(1990, 1, 1),
            username = "Applicant1",
            id = "user1",
            email = "applicant1@example.com",
            picture = "https://example.com/user1.jpg",
            jobs = listOf("job1", "job2"),
            preferredFields = listOf("Technology", "Design")
        ),
        User(
            dob = Date(1992, 2, 2),
            username = "Applicant2",
            id = "user2",
            email = "applicant2@example.com",
            picture = "https://example.com/user2.jpg",
            jobs = listOf("job3", "job4"),
            preferredFields = listOf("Marketing", "Sales")
        ),
        User(
            dob = Date(1985, 3, 3),
            username = "Applicant3",
            id = "user3",
            email = "applicant3@example.com",
            picture = "https://example.com/user3.jpg",
            jobs = listOf("job5", "job6"),
            preferredFields = listOf("Engineering", "Management")
        ),
        User(
            dob = Date(1995, 4, 4),
            username = "Applicant4",
            id = "user4",
            email = "applicant4@example.com",
            picture = "https://example.com/user4.jpg",
            jobs = listOf("job7", "job8"),
            preferredFields = listOf("Finance", "Healthcare")
        )
    )
    ConvHubTheme {
        JobDetailContent(mockJob, dummyUsers, null)
    }
}

