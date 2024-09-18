package edu.bluejack23_2.convhub.ui.screens.forgotpassword

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.material.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack23_2.convhub.R
import edu.bluejack23_2.convhub.ui.theme.ConvHubTheme

@AndroidEntryPoint
class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConvHubTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ForgotPasswordScreen()
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: ForgotPasswordViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var emailVerified by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.convhub_logo_only_blue),
            contentDescription = "ConvHub Logo",
            modifier = Modifier
                .size(200.dp)
        )
        Text(
            text = "ConvHub",
            style = MaterialTheme.typography.h4.copy(color = Color(0xFF266EC4)),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.h6.copy(color = Color.Black),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color(0xFF266EC4),
                unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.verifyEmail(email) { exists ->
                    if (exists) {
                        emailVerified = true
                        viewModel.sendPasswordResetEmail(email) { success ->
                            if (success) {
                                Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Email does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF266EC4))
        ) {
            Text("Verify Email", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (emailVerified) {
            Text(
                text = "Check your email for the password reset link.",
                color = Color(0xFF266EC4),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


