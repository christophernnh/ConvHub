package edu.bluejack23_2.convhub.ui.screens.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import edu.bluejack23_2.convhub.data.model.User
import edu.bluejack23_2.convhub.ui.events.UiEvent
import edu.bluejack23_2.convhub.ui.screens.detailLister.DetailListerScreen
import edu.bluejack23_2.convhub.ui.screens.detailTaker.DetailTakerScreen
import edu.bluejack23_2.convhub.ui.screens.user.UserViewModel
import edu.bluejack23_2.convhub.ui.theme.ConvHubTheme
import edu.bluejack23_2.convhub.ui.theme.DarkBlue
import edu.bluejack23_2.convhub.ui.theme.LightGray
import edu.bluejack23_2.convhub.ui.theme.PastelBlue
import kotlinx.coroutines.flow.collectLatest

data class JobItemData(
    val imageUrl: String,
    val title: String,
    val rating: String,
    val jobLister: String,
    val price: String
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val jobState by viewModel.jobState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val preferredFields by viewModel.preferredFields.collectAsState()
    val jobPreferred by viewModel.jobPreferred.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchJobs()
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ConvHubTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlue)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    currentUser = currentUser
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    if (jobState.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "No Data Available",
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "All Jobs",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val filteredJobs = jobState.filter { job ->
                                    job.title.contains(
                                        searchQuery.text,
                                        ignoreCase = true
                                    ) || job.description.contains(
                                        searchQuery.text,
                                        ignoreCase = true
                                    ) || job.categories.any {
                                        it.contains(
                                            searchQuery.text,
                                            ignoreCase = true
                                        )
                                    }
                                }
                                items(filteredJobs) { job ->
                                    JobItem(
                                        imageUrl = job.imageUris[0],
                                        title = job.title,
                                        rating = job.rating.toString(),
                                        author = job.jobLister,
                                        price = job.price.toString(),
                                        onClick = {
                                            if (currentUser?.username?.equals(job.jobLister) == true) {
                                                val intent = Intent(
                                                    context,
                                                    DetailListerScreen::class.java
                                                ).apply {
                                                    putExtra("jobId", job.id)
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                val intent = Intent(
                                                    context,
                                                    DetailTakerScreen::class.java
                                                ).apply {
                                                    putExtra("jobId", job.id)
                                                }
                                                context.startActivity(intent)
                                            }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (preferredFields.isEmpty()) {
                            item {
                                SelectPreferredFieldsScreen(
                                    availableFields = listOf(
                                        "Technology",
                                        "Cleaning",
                                        "Babysitting",
                                        "Housework"
                                    ),
                                    onFieldsSelected = { fields ->
                                        viewModel.savePreferredFields(fields)
                                    }
                                )
                            }
                        } else if (searchQuery.text.isEmpty() || searchQuery.text == "") {
                            item {
                                Text(
                                    text = "Recommended Jobs",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(jobPreferred) { job ->
                                        JobItem(
                                            imageUrl = job.imageUris.firstOrNull().toString(),
                                            title = job.title,
                                            rating = job.rating.toString(),
                                            author = job.jobLister,
                                            price = job.price.toString(),
                                            onClick = {

                                                if (currentUser?.username?.equals(job.jobLister) == true) {
                                                    val intent = Intent(
                                                        context,
                                                        DetailListerScreen::class.java
                                                    ).apply {
                                                        putExtra("jobId", job.id)
                                                    }
                                                    context.startActivity(intent)
                                                } else {
                                                    val intent = Intent(
                                                        context,
                                                        DetailTakerScreen::class.java
                                                    ).apply {
                                                        putExtra("jobId", job.id)
                                                    }
                                                    context.startActivity(intent)
                                                }

                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectPreferredFieldsScreen(
    availableFields: List<String>,
    onFieldsSelected: (List<String>) -> Unit
) {
    var selectedFields by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Select Preferred Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
            crossAxisAlignment = FlowCrossAxisAlignment.Center,
            modifier = Modifier.fillMaxWidth(),
            crossAxisSpacing = 8.dp
        ) {
            availableFields.forEach { field ->
                Button(
                    onClick = {
                        selectedFields = if (selectedFields.contains(field)) {
                            selectedFields - field
                        } else {
                            selectedFields + field
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedFields.contains(field)) DarkBlue else Color.White,
                        contentColor = if (selectedFields.contains(field)) Color.White else Color.Black
                    ),
                    elevation = ButtonDefaults.elevation(0.dp),
                    border = if (!selectedFields.contains(field)) {
                        BorderStroke(1.dp, Color.Black)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.48f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(text = field, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onFieldsSelected(selectedFields) },
            colors = ButtonDefaults.buttonColors(Color.Black),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text(text = "Save Preferences", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ConvHubTheme {
        HomeScreenPreviewContent()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun JobItem(
    imageUrl: String,
    title: String,
    rating: String,
    author: String,
    price: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(260.dp),
        border = BorderStroke(
            color = LightGray,
            width = 1.dp
        ),
        elevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = rememberImagePainter(imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight(500),
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )

            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp
                    )
                    .weight(0.3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        painter = painterResource(id = edu.bluejack23_2.convhub.R.drawable.icon_star),
                        contentDescription = "Star",
                        tint = Color.Yellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = rating)
                }
                Text(
                    text = author,
                    fontWeight = FontWeight(500)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp
                    )
                    .weight(0.3f),
            ) {
                Text(
                    text = "From ",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = String.format("$%s", price),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun HomeScreenPreviewContent() {
    // Simulate some initial state for preview

    val dummyJobs = listOf(
        JobItemData(
            imageUrl = "https://your-firebase-url.com/image1.jpg",
            title = "Clean a backyard of a House",
            rating = "4.5",
            jobLister = "roglau",
            price = "$50"
        ),
        JobItemData(
            imageUrl = "https://your-firebase-url.com/image2.jpg",
            title = "Babysitting for 2 hours",
            rating = "4.8",
            jobLister = "jane_doe",
            price = "$30"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopSection(
                searchQuery = TextFieldValue("Search..."),
                onSearchQueryChange = {},
                modifier = Modifier
                    .fillMaxWidth(),
                currentUser = null
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Nearest Jobs",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(dummyJobs) { job ->
                        JobItem(
                            imageUrl = job.imageUrl,
                            title = job.title,
                            rating = job.rating,
                            author = job.jobLister,
                            price = job.price,
                            onClick = {}
                        )
                    }
                }

                SelectPreferredFieldsScreen(
                    availableFields = listOf("Engineering", "Design", "Marketing", "Sales"),
                    onFieldsSelected = { fields ->
                        // userViewModel.updatePreferredFields(fields)
                        // viewModel.fetchJobsByPreferredFields(fields)
                    }
                )
            }
        }
    }
}

@Composable
fun TopSection(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    currentUser: User?
) {
    Column(
        modifier = modifier
            .padding(
                horizontal = 16.dp,
                vertical = 28.dp
            )
    ) {
        Text(
            text = String.format("Hello, %s!", currentUser?.username),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight(500),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(text = "Search Jobs", fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = PastelBlue,
            ),
            textStyle = TextStyle(fontSize = 12.sp),
            shape = RoundedCornerShape(14.dp),

            )
    }
}
