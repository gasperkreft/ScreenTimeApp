package com.example.myapplication

class Friend {
    var name: String = ""
    var email: String = ""
    var screenTime: Int = 0
    var uid: String = ""

    constructor()

    constructor(name: String, email: String, screenTime: Int, uid:String) {
        this.name = name
        this.email = email
        this.screenTime = screenTime
        this.uid = uid
    }
}