package com.example.myapplication

import android.app.ActivityOptions
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SignIn : ComponentActivity() {
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val user = auth.currentUser;
        if (user != null) {
            val intent = Intent(this, Test::class.java);
            startActivity(intent)
            finish()
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                SignInScreen(context = this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            reload()
        }
    }

    private fun reload() {

    }
}

@Composable
fun SignInScreen(modifier: Modifier = Modifier, context: SignIn) {
    var name by remember { mutableStateOf("")}
    var password by remember {mutableStateOf("")}

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxWidth().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center ){
                Text("Sign In", style = MaterialTheme.typography.headlineLarge)
                UsernameField(modifier = Modifier.fillMaxWidth().padding(top = 15.dp),{it -> name = it })
                PasswordField(modifier = Modifier.fillMaxWidth().padding(top = 15.dp,bottom = 15.dp),{it -> password = it})
                Button(modifier = Modifier.fillMaxWidth(),onClick = { signIn(context, name, password) }
            ) {
                    Text("Sign In")
                }
                Text("Don't have an account?", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    val intent = Intent(context, SignUp::class.java)
                    context.startActivity(intent) }) {
                    Text("Sign Up")
                }
        }
    }
}

fun signIn(context: SignIn, username: String, password: String) {
    // [START sign_in_with_email]
    val database = FirebaseDatabase.getInstance()
    val usernamesRef = database.getReference("usernames")
    usernamesRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val uid = snapshot.getValue(String::class.java)
            if (uid != null) {
                // User found, fetch user data from the 'users' node using the uid
                val usersRef = database.getReference("users")
                usersRef.child(uid).child("email").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val email = snapshot.getValue(String::class.java).toString()
                        context.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(context) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success")
                                    val user = context.auth.currentUser
                                    goToMain(user,context)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                                    Toast.makeText(context, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            } else {
                // User not found
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })

    // [END sign_in_with_email]
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameField(modifier: Modifier,onChangeFunction: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        modifier = modifier,
        onValueChange = {text = it; onChangeFunction(it)},
        label = { Text("Username") },
        shape = RoundedCornerShape(20)
    )
}

fun goToMain(user: FirebaseUser?, context: SignIn) {
    if (user != null) {
        val intent = Intent(context, Test::class.java)
        context.startActivity(intent)
        context.finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(modifier: Modifier,onChangeFunction:(String)->Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        modifier = modifier,
        onValueChange = { text = it; onChangeFunction(it)},
        label = { Text("Password") },
        shape = RoundedCornerShape(20)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    MyApplicationTheme {
        SignInScreen(context = SignIn())
    }
}