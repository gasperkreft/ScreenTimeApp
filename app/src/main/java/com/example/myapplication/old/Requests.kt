package com.example.myapplication.old

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.Request
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class Requests : ComponentActivity() {
    private val usernamesList = mutableListOf<String>()
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                RequestsScreen(context = this, modifier = Modifier.fillMaxSize())

                val database = Firebase.database
                val userRef = database.getReference("users/")
                val requestRef = database.getReference("requests/")
                val currentUser = FirebaseAuth.getInstance().currentUser

                val friendsRef = database.getReference("friends/"+ currentUser!!.uid)
                val friendsList = mutableListOf<String>()
                friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (friendSnapshot in snapshot.children) {
                            val friendUid = friendSnapshot.key
                            if(friendUid != null) {
                                friendsList.add(friendUid)
                                userRef.child(friendUid).child("name").addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val name = snapshot.getValue(String::class.java)
                                        if (name != null) {
                                            friendsList.add(name)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle any errors that occur
                                    }
                                })
                            }
                        }

                        val usernamesRef = database.getReference("usernames")
                        usernamesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot1: DataSnapshot) {
                                userRef.child(currentUser.uid).child("name").addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot2: DataSnapshot) {
                                        val currentName = snapshot2.getValue(String::class.java)
                                        for (usernameSnapshot in snapshot1.children) {
                                            val username = usernameSnapshot.key
                                            if (username != currentName && username !in friendsList) {
                                                if (username != null) {
                                                    usernamesList.add(username)
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle any errors that occur
                                    }
                                })

                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })


                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // handle errors here
                    }
                })
            }
        }
    }

    fun findPossibleFriends(string: String): List<String> {
        val query = string.lowercase()
        val filteredUsernames =
            usernamesList.filter { it.lowercase().contains(query) }.toMutableList()
        return filteredUsernames
    }
}

@Composable
fun RequestsScreen(modifier: Modifier = Modifier, context: Requests) {
    Column(modifier = modifier) {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    val options = ActivityOptions
                        .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left)
                    context.startActivity(intent, options.toBundle())
                }, modifier = Modifier.size(60.dp)) {
                    Icon(
                        modifier = Modifier.fillMaxSize(0.7f),
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "test"
                    )
                }
            }
        }
        Surface {
            Column(Modifier.fillMaxSize()) {
                SearchSurface(context)
                RequestsSurface(context)
            }
        }
    }
}


@Composable
fun SearchSurface(
    context: Requests
) {
    val names = remember { mutableStateListOf<String>()}

    Column(Modifier.padding(10.dp)) {
        Text("Add friends", style = MaterialTheme.typography.headlineLarge)
        SimpleFilledTextFieldSample { string ->
            run {
                names.clear()
                names.addAll((context::findPossibleFriends)(string))
            }
        }
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = names) { name ->
                AddFriendCard(name = name)
            }
        }
    }
}

@Composable
fun AddFriendCard(name:String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(name,
            Modifier
                .weight(1f)
                .padding(10.dp))
        var clicked by remember { mutableStateOf(false) }
        val BtnColor by animateColorAsState(
            if (clicked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        )
        Button(onClick = { Add(name);clicked = true},
                colors = ButtonDefaults.buttonColors(containerColor = BtnColor)
                ) {
            if (!clicked) { Text("Add")}
            else {Text("Added")}
        }
    }
}

fun Add(username:String) {
    if (username.isNotEmpty()) {
        val database = Firebase.database
        val userRef = database.getReference("users/")
        val requestRef = database.getReference("requests/")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val query = userRef.orderByChild("name").equalTo(username)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    // Do something with the matching node
                    val friend_uid = snapshot.key

                    if (friend_uid != null) {
                        //val friendRef = database.getReference("friends/${currentUser?.uid}")
                        //friendRef.child(friend_uid).setValue(true)
                        if (currentUser != null) {
                            requestRef.child(friend_uid).child(currentUser.uid).setValue(true)

                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //
            }
        }

        query.addListenerForSingleValueEvent(valueEventListener)
    }
}

@Composable
private fun RequestsSurface(
    context: Requests
) {
    var requests = remember { mutableStateListOf<Request>()}

    val database = FirebaseDatabase.getInstance()
    val currentUser = context.auth.currentUser
    val friendsRef = database.getReference("friends/"+currentUser!!.uid)
    val requestRef = database.getReference("requests/")
    val userRef = database.getReference("users/")

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            val query = requestRef.child(currentUser.uid).orderByKey()
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestList = mutableListOf<Request>()
                    for (requestSnapshot in snapshot.children) {
                        val fromUid = requestSnapshot.key ?: continue
                        userRef.child(fromUid).child("name").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val name = snapshot.getValue(String::class.java)
                                // Do something with the retrieved string data
                                val request = Request(fromUid, currentUser.uid, name ?: "")
                                requests.add(request)

                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle any errors that occur
                            }
                        })

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // handle errors here
                }
            })
        }
    }

    Column(Modifier.padding(10.dp)) {
        Text("Requests", style = MaterialTheme.typography.headlineLarge)
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = requests) { request ->
                RequestCard(request = request,requests)
            }
        }
    }
}

@Composable
fun RequestCard(request: Request,requests: SnapshotStateList<Request>) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(request.fromName,
            Modifier
                .weight(1f)
                .padding(10.dp))
        Button(onClick = {Accept(request,requests)}) {
            Text("Accept")
        }
    }
}

fun Accept(request: Request, requests: SnapshotStateList<Request>) {
    val database = FirebaseDatabase.getInstance()
    val requestsRef = database.getReference("requests")
    val friendRef1 = database.getReference("friends/${request.from}")
    friendRef1.child(request.to).setValue(true)
    val friendRef2 = database.getReference("friends/${request.to}")
    friendRef2.child(request.from).setValue(true)
    requestsRef.child(request.to).child(request.from).removeValue()
    requests.remove(request)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleFilledTextFieldSample(onChangeFunction:(String)->Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { text = it;onChangeFunction(it) },
        placeholder = { Text("Username") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon")},
        shape = RoundedCornerShape(20)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    MyApplicationTheme {
        RequestsScreen(context = Requests(), modifier = Modifier.fillMaxSize())
    }
}