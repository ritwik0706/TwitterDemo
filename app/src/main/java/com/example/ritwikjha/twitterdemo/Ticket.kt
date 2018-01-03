package com.example.ritwikjha.twitterdemo

class Ticket{
    var tweetID:String?=null
    var tweetText:String?=null
    var tweetImgURL:String?=null
    var tweetPersonalUID:String?=null

    constructor(tweetID:String,tweetText:String,tweetImgURL:String,tweetPersonalUID:String){
        this.tweetID=tweetID
        this.tweetText=tweetText
        this.tweetImgURL=tweetImgURL
        this.tweetPersonalUID=tweetPersonalUID
    }
}
