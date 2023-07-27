package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance()
    private val user = Firebase.auth.currentUser

    val usernamesRef = database.getReference("usernames")
    val userRef = database.getReference("users/")

    private val dataList = MutableLiveData<List<String>>()

    init {
        fetchData()
    }

    private fun fetchData() {
        val database = Firebase.database
        val userRef = database.getReference("users/")
        val currentUser = FirebaseAuth.getInstance().currentUser

        val friendsRef = database.getReference("friends/"+ currentUser!!.uid)
        val friendsList = mutableListOf<String>()
        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (friendSnapshot in snapshot.children) {
                    val friendUid = friendSnapshot.key
                    if(friendUid != null) {
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
                                val usernamesList = mutableListOf<String>()
                                val currentName = snapshot2.getValue(String::class.java)
                                for (usernameSnapshot in snapshot1.children) {
                                    val username = usernameSnapshot.key
                                    if (username != currentName && username !in friendsList) {
                                        if (username != null) {
                                            usernamesList.add(username)
                                        }
                                    }
                                }
                                dataList.value = usernamesList
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


    fun getDataList(): LiveData<List<String>> {
        return dataList
    }
}