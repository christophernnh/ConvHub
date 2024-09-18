package edu.bluejack23_2.convhub

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import edu.bluejack23_2.convhub.databinding.ActivityRegisterBinding
import java.util.Calendar
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.alreadyAccountView.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        binding.dobEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.passwordEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val strength = evaluatePasswordStrength(password)
                binding.passwordStrengthTextView.text = "Password Strength: $strength"
                binding.passwordStrengthTextView.visibility = View.VISIBLE
                when (strength) {
                    "Weak" -> binding.passwordStrengthTextView.setTextColor(Color.RED)
                    "Medium" -> binding.passwordStrengthTextView.setTextColor(Color.YELLOW)
                    "Strong" -> binding.passwordStrengthTextView.setTextColor(Color.GREEN)
                    "Very Strong" -> binding.passwordStrengthTextView.setTextColor(Color.BLUE)
                    else -> binding.passwordStrengthTextView.setTextColor(Color.GRAY)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val dob = binding.dobEditText.text.toString()

            if (!isEmailValid(email)) {
                Toast.makeText(this, "Email field must be filled in correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isUsernameValid(username)) {
                Toast.makeText(this, "Username field can only consist of alphanumeric characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var isUnique:Boolean = true
            firestore.collection("users").whereEqualTo("username", username).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Toast.makeText(this, "Username is already taken", Toast.LENGTH_SHORT).show()
                        isUnique = false
                        return@addOnSuccessListener
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking username: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addOnFailureListener
                }

            if(!isUnique){
                return@setOnClickListener
            }

            if (password.length < 8 || !isPasswordValid(password)) {
                Toast.makeText(this, "Password field must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dob.equals("")) {
                Toast.makeText(this, "Date of Birth must be selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidAge(dob)) {
                Toast.makeText(this, "You must be at least 18 years old to register", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(){
                if (it.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        saveUserToFirestore(user.uid, username, email, dob)
                    }
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    return@addOnCompleteListener
                }

                Toast.makeText(this, "Error registering!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.dobEditText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isUsernameValid(username: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]*$")
        return pattern.matcher(username).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return evaluatePasswordStrength(password) == "Strong" || evaluatePasswordStrength(password) == "Very Strong"
    }


    private fun evaluatePasswordStrength(password: String): String {
        var strengthPoints = 0

        // Check if password is at least 8 characters long
        if (password.length >= 8) strengthPoints++

        // Check if password has alphabetic characters
        if (password.any { it.isLetter() }) strengthPoints++

        // Check if password has numeric characters
        if (password.any { it.isDigit() }) strengthPoints++

        // Check if password has special characters
        val specialCharacters = "!@#\$%^&*()-_=+[]{}|;:'\",<.>/?"
        if (password.any { specialCharacters.contains(it) }) strengthPoints++

        return when (strengthPoints) {
            1 -> "Weak"
            2 -> "Medium"
            3 -> "Strong"
            4 -> "Very Strong"
            else -> "Very Weak"
        }
    }


    private fun isValidAge(dob: String): Boolean {
        val parts = dob.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        val dobCalendar = Calendar.getInstance()
        dobCalendar.set(year, month - 1, day)

        val today = Calendar.getInstance()
        val age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            return age - 1 >= 18
        }
        return age >= 18
    }


    private fun saveUserToFirestore(uid: String, username: String, email: String, dob: String) {
        val parts = dob.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        val dobCalendar = Calendar.getInstance()
        dobCalendar.set(year, month - 1, day)

        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "dob" to dobCalendar.time
        )

        firestore.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}