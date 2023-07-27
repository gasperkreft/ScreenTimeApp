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

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance()
    private val user = Firebase.auth.currentUser

    val friendsRef = database.getReference("friends/"+user!!.uid)
    val userRef = database.getReference("users/")

    private val dataList = MutableLiveData<List<Friend>>()

    init {
        fetchData()
    }

    private fun fetchData() {
        friendsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendsList = mutableListOf<Friend>()
                val totalFriends  = snapshot.childrenCount
                var processedFriends:Long = 0
                for (friendSnapshot in snapshot.children) {
                    val friendUid = friendSnapshot.key ?: continue
                    userRef.child(friendUid).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val friend = userSnapshot.getValue(Friend::class.java) ?: return
                            friendsList.add(friend)
                            processedFriends++

                            if (processedFriends == totalFriends) {
                                dataList.value = friendsList
                            }
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

    fun getDataList(): LiveData<List<Friend>> {
        return dataList
    }
}