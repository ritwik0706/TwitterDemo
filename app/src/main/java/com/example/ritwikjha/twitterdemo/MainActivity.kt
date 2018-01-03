package com.example.ritwikjha.twitterdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweets.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    var database=FirebaseDatabase.getInstance()
    var myRef=database.reference

    var listOfTweets=ArrayList<Ticket>()
    var adapter:MyTweetsAdapter?=null

    var myEmail:String?=null
    var myUID:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var b:Bundle=intent.extras
        myEmail=b.getString("email")
        myUID=b.getString("uid")


        listOfTweets.add(Ticket("0","Text","Image Url","add"))
       // listOfTweets.add(Ticket("0","Text","Image Url","fd98d7f9sdf"))
        //listOfTweets.add(Ticket("0","Text","Image Url","asdsd876q3"))

        adapter= MyTweetsAdapter(this,listOfTweets)
        lvTweets.adapter=adapter

        LoadPosts()
    }

    override fun onResume() {
        super.onResume()
        LoadPosts()
    }

     inner class MyTweetsAdapter:BaseAdapter{

        var listoftweetsadapter=ArrayList<Ticket>()
        var context:Context?=null

        constructor(context:Context,listoftweetsadapter:ArrayList<Ticket>){
            this.context=context
            this.listoftweetsadapter=listoftweetsadapter
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var myTweet=listoftweetsadapter[position]
            var inflator=context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            if (myTweet.tweetPersonalUID.equals("add")){

                var myView=inflator.inflate(R.layout.add_ticket,null)

                myView.ivAttach.setOnClickListener(View.OnClickListener {
                    LoadImage()
                })

                myView.ivPostButton.setOnClickListener(View.OnClickListener {

                    myRef.child("Posts").push().setValue(PostInfo(myUID!!,
                            myView.etPost.text.toString(),
                            DownloadURL!!))
                })

                return myView
            }else{

                var myView=inflator.inflate(R.layout.tweets,null)
                myView.tvTweetPost.text=myTweet.tweetText
                myView.tvUsername.text=myTweet.tweetPersonalUID
                Picasso.with(context).load(myTweet.tweetImgURL).into(myView.ivPost)

                myRef.child("Users").child(myTweet.tweetPersonalUID)
                        .addValueEventListener(object:ValueEventListener{

                            override fun onDataChange(p0: DataSnapshot?) {

                                try {
                                    var td=p0!!.value as HashMap<String,Any>

                                    for (key in td.keys){
                                        var userInfo=td[key] as String

                                        if (key == "Profile Image"){
                                            Picasso.with(context).load(userInfo).into(myView.ivUser)
                                        }else{
                                            myView.tvUsername.text=userInfo
                                        }
                                    }
                                }catch (ex:Exception){}
                            }

                            override fun onCancelled(p0: DatabaseError?) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }
                        })


                return myView
            }
        }

        override fun getItem(position: Int): Any {
            return listoftweetsadapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listoftweetsadapter.size
        }

    }

    val imgCode=123
    fun LoadImage(){
        var intent=Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,imgCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==imgCode && data!=null && resultCode== Activity.RESULT_OK){

            val selectedImage=data.data
            val filePathCol= arrayOf(MediaStore.Images.Media.DATA)
            var cursor=contentResolver.query(selectedImage,filePathCol,null,null,null)
            cursor.moveToFirst()
            val colIndex=cursor.getColumnIndex(filePathCol[0])
            val picturepath=cursor.getString(colIndex)
            cursor.close()
            UploadImage(BitmapFactory.decodeFile(picturepath))
        }
    }

    var DownloadURL:String?=""
    fun UploadImage(bitmap: Bitmap){

        val storage=FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitter-8160b.appspot.com")
        val df=SimpleDateFormat("ddMMyyHHmmss")
        val dataobj=Date()
        val imgpath=SplitString(myEmail!!)+"."+df.format(dataobj)+".jpg"
        val imgRef=storageRef.child("PostImage/"+imgpath)
        var baos=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        var data=baos.toByteArray()
        val uploadTask=imgRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"Upload Failed",Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->

            DownloadURL=taskSnapshot.downloadUrl!!.toString()

        }

    }


    fun SplitString(str:String):String{
        var split=str.split("@")
        return split[0]
    }

    fun LoadPosts(){
        myRef.child("Posts")
                .addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(p0: DataSnapshot?) {

                        try {

                            listOfTweets.clear()
                            listOfTweets.add(Ticket("0","Text","Image Url","add"))

                            var td=p0!!.value as HashMap<String,Any>

                            for (key in td.keys){
                                var post=td[key] as HashMap<String,Any>

                                listOfTweets.add(Ticket(key,
                                        post["txt"] as String,
                                        post["postImg"] as String,
                                        post["userUID"] as String))

                                adapter!!.notifyDataSetChanged()
                            }
                        }catch (ex:Exception){}
                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
    }

}
