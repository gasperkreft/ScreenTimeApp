package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SignUp : ComponentActivity() {
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignUpScreen(context = this)
                }
            }
        }
    }
}
@Composable
fun SignUpScreen(modifier: Modifier = Modifier, context: SignUp) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxWidth().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center ){
                Text("Sign Up", style = MaterialTheme.typography.headlineLarge)
                EmailField(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),{it -> email = it })
                UsernameField(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),{it -> name = it })
                PasswordField(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 15.dp),{it -> password = it})
                Button(modifier = Modifier.fillMaxWidth(),onClick = { signUp(context,email, name, password) }
                ) {
                    Text("Sign Up")
                }
                Text("Already have an account?", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(modifier = Modifier.fillMaxWidth(),onClick = {
                    val intent = Intent(context, SignIn::class.java)
                    context.startActivity(intent)
                }) {
                    Text("Sign In")
                }
        }
    }
}
fun signUp(context: SignUp, email : String,username: String, password: String) {
    val database = FirebaseDatabase.getInstance()
    val usernamesRef = database.getReference("usernames")
    usernamesRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                // Username already exists, display an error message
                Toast.makeText(
                    context, "Username already exists",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                context.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(context) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = context.auth.currentUser
                            val database = FirebaseDatabase.getInstance()
                            var ref = database.getReference("users/")
                            val userData = user?.let { Friend(username, email, 0, it.uid) }
                            if (user != null) {
                                ref.child(user.uid).setValue(userData)
                                ref = database.getReference("usernames/")
                                ref.child(username).setValue(user.uid)
                            }

                            val intent = Intent(context, Test::class.java)
                            context.startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(SignUp.TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                context, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            //Handle error
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailField(modifier: Modifier,onChangeFunction:(String)->Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        modifier = modifier,
        onValueChange = { text = it; onChangeFunction(it)},
        label = { Text("Email") },
        shape = RoundedCornerShape(20)
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview7() {
    MyApplicationTheme {
        SignUpScreen(context = SignUp())
    }
}