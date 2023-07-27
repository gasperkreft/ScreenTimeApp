package com.example.myapplication

import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

class Settings : ComponentActivity() {
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
                    SettingsScreen(context = this)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, context: Settings) {
    Column(modifier = modifier) {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(context, Test::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    val options = ActivityOptions
                        .makeCustomAnimation(context, R.anim.slide_in_left, R.anim.slide_out_right)
                    context.startActivity(intent, options.toBundle())
                }, modifier = Modifier.size(60.dp)) {
                    Icon(
                        modifier = Modifier.fillMaxSize(0.7f),
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "test"
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    FirebaseAuth.getInstance().signOut();
                    val intent = Intent(context, SignIn::class.java)
                    val options = ActivityOptions
                        .makeCustomAnimation(context, R.anim.slide_in_left, R.anim.slide_out_right)
                    context.startActivity(intent, options.toBundle())
                    context.finish()

                }, modifier = Modifier.size(60.dp)) {
                    Icon(
                        modifier = Modifier.fillMaxSize(0.7f),
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "test"
                    )
                }
            }
        }
        Surface {
            Column(Modifier.fillMaxSize()) {
                ProfileCard(context)
            }
        }
    }
}
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileCard(context:Settings ){
    Column(modifier = Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
        var imageUri by remember {
            mutableStateOf<Uri?>(null)
        }

        val user = context.auth.currentUser
        val storageRef = FirebaseStorage.getInstance().getReference("images")
        val nameRef =
            FirebaseDatabase.getInstance().getReference("users").child(user!!.uid).child("image")


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
                .size(200.dp)
                .clip(CircleShape))

        val launcher = rememberLauncherForActivityResult(contract =
        ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri

            // Do something with the selected image URI
            val imageRef = storageRef.child(imageUri?.lastPathSegment!!)
            val imageFile = File(imageUri?.path!!)
            val imageName = imageFile.name
            val uploadTask = imageRef.putFile(imageUri!!)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d(ContentValues.TAG, "Upload is $progress% done")
            }


            val user = context.auth.currentUser

            FirebaseDatabase.getInstance().getReference("users").child(user!!.uid).child("image").setValue(imageName)
        }

        Button(onClick = {launcher.launch("image/*")
        }) {
            Text("Change")
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MyImage(context:Settings) {

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    MyApplicationTheme {
        SettingsScreen(context = Settings())
    }
}