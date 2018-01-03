package com.example.ritwikjha.twitterdemo

class PostInfo{
    var UserUID:String?=null
    var txt:String?=null
    var postImg:String?=null

    constructor(UserUID:String,txt:String, postImg:String){
        this.UserUID=UserUID
        this.txt=txt
        this.postImg=postImg
    }
}