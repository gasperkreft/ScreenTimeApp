package com.example.myapplication

import android.app.ActivityOptions
import android.app.Application
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.old.AddFriendCard
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Requests2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Request2Screen(context = this)
                }
            }
        }
    }
}

fun findPossibleFriends(dataList:List<String>,string: String): List<String>? {
    val query = string.lowercase()
    val filteredUsernames = dataList.filter { it.lowercase().contains(query) }
    return filteredUsernames
}

@Composable
fun Request2Screen(modifier: Modifier = Modifier, context: Requests2) {
    Column(modifier = modifier) {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val intent = Intent(context, Test::class.java)
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
                val searchViewModel: SearchViewModel = viewModel()
                SearchSurface2(searchViewModel)
                val requestViewModel: RequestsViewModel = viewModel()
                RequestsSurface2(requestViewModel)
            }
        }
    }
}

@Composable
fun SearchSurface2(viewModel: SearchViewModel) {
    val dataList by viewModel.getDataList().observeAsState(emptyList())
    val names = remember { mutableStateListOf<String>()}

    Column(Modifier.padding(10.dp)) {
        Text("Add friends", style = MaterialTheme.typography.headlineLarge)
        SimpleFilledTextField { string ->
            run {
                names.clear()
                findPossibleFriends(dataList,string)?.let { names.addAll(it) }
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
private fun RequestsSurface2(viewModel: RequestsViewModel) {
    val dataList by viewModel.getDataList().observeAsState(emptyList())


    Column(Modifier.padding(10.dp)) {
        Text("Requests", style = MaterialTheme.typography.headlineLarge)
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = dataList) { request :Request ->
                RequestCard2(request = request,viewModel)
            }
        }
    }
}

fun Accept2(request: Request, viewModel: RequestsViewModel) {
    val database = FirebaseDatabase.getInstance()
    val requestsRef = database.getReference("requests")
    val friendRef1 = database.getReference("friends/${request.from}")
    friendRef1.child(request.to).setValue(true)
    val friendRef2 = database.getReference("friends/${request.to}")
    friendRef2.child(request.from).setValue(true)
    requestsRef.child(request.to).child(request.from).removeValue()
    viewModel.fetchData()

}


@Composable
fun RequestCard2(request: Request,viewModel: RequestsViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(request.fromName,
            Modifier
                .weight(1f)
                .padding(10.dp))
        Button(onClick = {Accept2(request,viewModel)}) {
            Text("Accept")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleFilledTextField(onChangeFunction:(String)->Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        modifier= Modifier.fillMaxWidth(),
        value = text,
        onValueChange = { text = it;onChangeFunction(it) },
        placeholder = { Text("Username") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon")},
        shape = RoundedCornerShape(20)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview6() {
    MyApplicationTheme {
        Request2Screen(context = Requests2())
    }
}