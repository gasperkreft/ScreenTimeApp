package com.example.myapplication

import android.app.ActivityOptions
import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.myapplication.old.goToRequests
import com.example.myapplication.old.goToSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class Test : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateScreenTime()
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestScreen(Modifier.fillMaxSize(),this)
                }
            }
        }
    }

    private fun getUsageStats(): Long {

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var totalTime = 0L
        var lastEventTime = 0L

        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.packageName == "com.instagram.android") {
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> lastEventTime = event.timeStamp
                    UsageEvents.Event.ACTIVITY_PAUSED -> totalTime += event.timeStamp - lastEventTime
                    //UsageEvents.Event.ACTIVITY_STOPPED -> {totalTime += event.timeStamp - lastEventTime;lastEventTime = event.timeStamp}
                }
                Log.d(TAG, "${event.timeStamp}:${event.eventType}")
            }
        }
        return totalTime
    }

    fun updateScreenTime() : Long {
        val screenTimeMillis = getUsageStats()

        val database = FirebaseDatabase.getInstance()
        val auth = Firebase.auth
        val userRef = database.getReference("users/${auth.currentUser?.uid}")

        val screenTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(screenTimeMillis)
        userRef.child("screenTime").setValue(screenTimeMinutes)
        return screenTimeMinutes
    }

}


@Composable
fun TestScreen(modifier: Modifier = Modifier,context:Test) {
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
                Column(Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = "${context.updateScreenTime()} min")
                }
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

            val viewModel: FriendsViewModel = viewModel()
            YourScreen(viewModel)
        }
    }
}

@Composable
fun YourScreen(viewModel: FriendsViewModel) {
    val dataList by viewModel.getDataList().observeAsState(emptyList())

    LazyColumn {
        items(items = dataList) { item ->
            FriendCard(item)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendCard(friend: Friend) {

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10),
    ) {
        Row(modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = friend.name,modifier = Modifier
                .padding(15.dp)
                .weight(1f))
            Text(text="${friend.screenTime.toString()} min",
                modifier = Modifier.padding(15.dp),
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}

/*
@Composable
fun MyChart() {
    val chartEntryModel = entryModelOf(4f, 12f, 8f, 16f)

    Chart(
        chart = lineChart(),
        model = chartEntryModel,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
    )
}

 */




@Preview(showBackground = true)
@Composable
fun DefaultPreview5() {
    MyApplicationTheme {
        MyApplicationTheme {
            Surface {
                //MyChart()
            }
            // A surface container using the 'background' color from the theme
        }
    }
}