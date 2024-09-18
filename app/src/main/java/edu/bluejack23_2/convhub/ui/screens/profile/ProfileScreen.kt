package edu.bluejack23_2.convhub.ui.screens.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import edu.bluejack23_2.convhub.R
import edu.bluejack23_2.convhub.ui.viewmodel.ProfileViewModel
import java.util.Calendar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()

    var username by remember { mutableStateOf(userState.username) }
    var email by remember { mutableStateOf(userState.email) }
    var dob by remember { mutableStateOf(userState.dob) }
    var picture by remember { mutableStateOf(userState.picture) }
    var jobs by remember { mutableStateOf(userState.jobs.joinToString(", ")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordStrength by remember { mutableStateOf("") }


    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    dob.let {
        calendar.time = it
    }

    LaunchedEffect(userState) {
        userState.let { user ->
            username = user.username
            email = user.email
            dob = user.dob
            picture = user.picture
            jobs = user.preferredFields.joinToString(", ")
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(year, month, day)
            dob = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Profile",
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
                    .clickable {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        imagePickerLauncher.launch(intent)
                    },
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri ?: picture),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .clickable {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        imagePickerLauncher.launch(intent)
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = dob.toString(),
            onValueChange = { /* Read-only */ },
            label = { Text("Date of Birth") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = jobs,
            onValueChange = { jobs = it },
            label = { Text("Preferred Fields (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val jobsList = jobs.split(",").map { it.trim() }
                    viewModel.updateUserProfile(
                        username,
                        email,
                        dob,
                        picture,
                        jobsList,
                        imageUri,
                        onSuccess = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        })
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Blue,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
            ) {
                Text("Save")
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordStrength = viewModel.evaluatePasswordStrength(it)
            },
            label = { Text("Change Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Password Strength: $passwordStrength",
            color = when (passwordStrength) {
                "Very Weak" -> Color.Red
                "Weak" -> Color.Red
                "Medium" -> Color.Yellow
                "Strong" -> Color.Green
                "Very Strong" -> Color.Green
                else -> Color.Gray
            },
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.changePassword(password, onSuccess = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }, onFailure = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                })
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Blue,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Change Password")
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
