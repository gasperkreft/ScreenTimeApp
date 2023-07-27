package com.example.myapplication.old

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.myapplication.Friend
import com.example.myapplication.R
import com.example.myapplication.Requests2
import com.example.myapplication.Settings
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : ComponentActivity() {
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth


        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                MyApp(modifier = Modifier.fillMaxSize(), context = this)
            }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier = Modifier, context: MainActivity) {
    Column(modifier = modifier,) {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {goToRequests(context)},modifier = Modifier.size(60.dp)) {
                    Icon(
                        modifier = Modifier.fillMaxSize(0.7f),
                        imageVector = Icons.Filled.Search,
                        contentDescription = "test"

                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { goToSettings(context) }, modifier = Modifier.size(60.dp)) {
                    Icon(
                        modifier = Modifier.fillMaxSize(0.7f),
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "test"
                    )
                }
            }
        }

        Surface {
            Leaderboard(modifier,context)
        }
    }
}

@Composable
private fun Leaderboard(
    modifier: Modifier = Modifier,
    context: MainActivity
) {
    val friends = mutableListOf<Friend>()
    val database = FirebaseDatabase.getInstance()
    val user = context.auth.currentUser
    val friendsRef = database.getReference("friends/"+user!!.uid)
    val userRef = database.getReference("users/")

    LaunchedEffect(Unit) {
        friendsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendsList = mutableListOf<Friend>()
                for (friendSnapshot in snapshot.children) {
                    val friendUid = friendSnapshot.key ?: continue
                    userRef.child(friendUid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val friend = userSnapshot.getValue(Friend::class.java) ?: return
                            friends.add(friend)
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            // handle errors here
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // handle errors here
            }
        })
    }

    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = friends) { friend ->
            Greeting(friend = friend,context)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
private fun Greeting(friend: Friend,context: MainActivity) {

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10),
    ) {
        Row(modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
            ) {

            var imageUri by remember {
                mutableStateOf<Uri?>(null)
            }

            val storageRef = FirebaseStorage.getInstance().getReference("images")
            val nameRef = FirebaseDatabase.getInstance().getReference("users").child(friend.uid).child("image")
            LaunchedEffect(Unit) {
                var imageRef: StorageReference? = null
                nameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imgName = snapshot.getValue(String::class.java)
                        imageRef = imgName?.let { storageRef.child(it) }
                        imageRef?.downloadUrl?.addOnSuccessListener { uri ->
                            imageUri = uri
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

            GlideImage(model = imageUri,
                contentDescription = "profile image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape))

            Text(text = friend.name,modifier = Modifier
                .padding(15.dp)
                .weight(1f))
            Text(text=friend.screenTime.toString(),
                modifier = Modifier.padding(15.dp),
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}

fun goToRequests(context: Context) {
    val intent = Intent(context, Requests2::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    val options = ActivityOptions
        .makeCustomAnimation(context, R.anim.slide_in_left, R.anim.slide_out_right)
    // Start the new activity
    context.startActivity(intent, options.toBundle())
}

fun goToSettings(context: Context) {
    val intent = Intent(context, Settings::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    val options = ActivityOptions
        .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left)
    // Start the new activity
    context.startActivity(intent, options.toBundle())
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Leaderboard(Modifier.fillMaxSize(),MainActivity())
    }
}

@Preview
@Composable
fun MyAppPreview() {
    MyApplicationTheme {
        MyApp(Modifier.fillMaxSize(), MainActivity())
    }
}