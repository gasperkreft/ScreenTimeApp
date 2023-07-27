package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class RequestsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance()
    private val user = Firebase.auth.currentUser

    val usernamesRef = database.getReference("usernames")
    val userRef = database.getReference("users/")
    val requestRef = database.getReference("requests/")

    private val dataList = MutableLiveData<List<Request>>()

    init {
        fetchData()
    }

    fun fetchData() {
        val query = requestRef.child(user!!.uid).orderByKey()
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestList = mutableListOf<Request>()
                val totalRequests  = snapshot.childrenCount
                var processedRequests:Long = 0
                if(totalRequests>0) {
                    for (requestSnapshot in snapshot.children) {
                        val fromUid = requestSnapshot.key ?: continue
                        userRef.child(fromUid).child("name")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val name = snapshot.getValue(String::class.java)
                                    // Do something with the retrieved string data
                                    val request = Request(fromUid, user.uid, name ?: "")
                                    requestList.add(request)
                                    processedRequests++

                                    if (processedRequests == totalRequests) {
                                        dataList.value = requestList
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle any errors that occur
                                }
                            })
                    }
                } else {
                    dataList.value = listOf()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // handle errors here
            }
        })
    }


    fun getDataList(): LiveData<List<Request>> {
        return dataList
    }
}