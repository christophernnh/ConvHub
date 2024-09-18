package edu.bluejack23_2.convhub

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack23_2.convhub.databinding.ActivityLoginBinding
import edu.bluejack23_2.convhub.ui.screens.forgotpassword.ForgotPasswordActivity
import edu.bluejack23_2.convhub.ui.screens.jobtakerprofile.JobTakerProfileActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding;
    private lateinit var firebaseAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(binding.root);

        binding.registerBtn.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        binding.signInButton.setOnClickListener{
            val emailText = binding.emailEditText.text.toString()
            var passwordText = binding.passwordEditText.text.toString()

            if (emailText.equals("") || passwordText.equals("")) {
                Toast.makeText(this, "Email and Password field must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener{
                if (it.isSuccessful) {
                    val homeIntent = Intent(this, HomeActivity::class.java)
                    startActivity(homeIntent);
                    return@addOnCompleteListener
                }

                Toast.makeText(this, "Error oh no!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgotPwLabel.setOnClickListener {
            val forgotPasswordIntent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordIntent)
        }

        val text : String = "Don't have an account? Register now"
        val spannableString = SpannableString(text)
        val registerNowColor = ForegroundColorSpan(Color.BLUE)  // Change to desired color

        val start = text.indexOf("Register now")
        val end = start + "Register now".length
        spannableString.setSpan(registerNowColor, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.registerBtn.text = spannableString
    }
}