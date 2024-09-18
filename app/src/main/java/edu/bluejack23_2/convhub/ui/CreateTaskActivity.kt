package edu.bluejack23_2.convhub.ui

import android.content.Context
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack23_2.convhub.R
import edu.bluejack23_2.convhub.data.repository.UserRepository
import edu.bluejack23_2.convhub.di.RepositoryModule
import edu.bluejack23_2.convhub.ui.theme.ConvHubTheme

class CreateTaskActivity : ComponentActivity() {


    private val userRepository: UserRepository = RepositoryModule.provideUserRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConvHubTheme() {
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    CreateScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun CreateScreen() {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            imageUris = uris
        }

    if (showDialog) {
        CustomAlertDialog(
            onDismissRequest = { showDialog = false },
            title = "Confirm",
            text = "Are you sure you want to upload this job?",
            onConfirm = {
                showDialog = false
                val priceInt = price.toIntOrNull()
                if (priceInt == null || priceInt <= 0) {
                    Toast.makeText(
                        context, "Price must be a positive number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@CustomAlertDialog
                }

                val job = hashMapOf(
                    "title" to title,
                    "address" to address,
                    "description" to description,
                    "price" to priceInt,
                    "categories" to categories.split(",").map { it.trim() },
                    "imageUris" to imageUris.map { it.toString() },
                    "posted_at" to Timestamp.now(),
                    "status" to "untaken",
                )

                uploadImagesAndSaveJob(imageUris, job, context) {
                    imageUris = emptyList()
                    title = ""
                    address = ""
                    description = ""
                    price = ""
                    categories = ""
                    errorMessage = ""

                    (context as ComponentActivity).finish()
                }
            },
            onDismiss = { showDialog = false }
        )
    }

    ConvHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    "Create Job",
                    fontSize = 24.sp,
                    fontWeight = FontWeight(600)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Step 1. Upload Images")
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    mainAxisSpacing = 10.dp,
                    crossAxisSpacing = 10.dp
                ) {
                    if (imageUris.isEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.convhub_logo_only_white),
                            contentDescription = "Placeholder Image",
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Gray, shape = RoundedCornerShape(5.dp))
                                .clickable {
                                },
                            contentScale = ContentScale.Inside
                        )
                        Image(
                            painter = painterResource(id = R.drawable.convhub_logo_only_white),
                            contentDescription = "Placeholder Image",
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Gray, shape = RoundedCornerShape(5.dp))
                                .clickable {
                                },
                            contentScale = ContentScale.Inside
                        )
                    } else {
                        imageUris.forEach { uri ->
                            val painter: Painter = rememberAsyncImagePainter(uri)
                            Image(
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(100.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = { launcher.launch("image/*") },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            backgroundColor = Color.Gray
                        ),
                        modifier = Modifier
                            .size(100.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_icon),
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Step 2. Add Job Details")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(30.dp))

                Text("Step 3. Specify Job Categories")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = categories,
                    onValueChange = { categories = it },
                    label = { Text("Categories (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (imageUris.isEmpty() || title.isEmpty() || address.isEmpty() || description.isEmpty() || price.isEmpty() || categories.isEmpty()) {
                            errorMessage =
                                "Please fill in all fields and select images from gallery"
                            Toast.makeText(
                                context, errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val priceInt = price.toIntOrNull()
                        if (priceInt == null || priceInt <= 0) {
                            errorMessage = "Price must be a positive number"
                            Toast.makeText(
                                context, errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        errorMessage = ""
                        showDialog = true
                    },
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        backgroundColor = Color.Blue
                    )
                ) {
                    Text(text = "Upload Job")
                }
            }
        }
    }
}

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("No")
                }
            }
        )
    }
}

fun uploadImagesAndSaveJob(
    imageUris: List<Uri>,
    job: HashMap<String, Any>,
    context: Context,
    onSuccess: () -> Unit
) {
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        return
    }

    val userId = currentUser.uid
    job["jobLister"] = userId

    val uploadedImageUrls = mutableListOf<String>()
    var uploadCount = 0

    imageUris.forEach { uri ->
        val imageReference = storageReference.child("images/" + uri.lastPathSegment)
        val uploadTask = imageReference.putFile(uri)

        uploadTask.addOnSuccessListener {
            imageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                uploadedImageUrls.add(downloadUri.toString())
                uploadCount++

                if (uploadCount == imageUris.size) {
                    // All images have been uploaded
                    job["imageUris"] = uploadedImageUrls

                    db.collection("job")
                        .add(job)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context, "Job Upload Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSuccess()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context, "Job Upload Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context, "Failed to get download URL for image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            Toast.makeText(
                context, "Image Upload Failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


