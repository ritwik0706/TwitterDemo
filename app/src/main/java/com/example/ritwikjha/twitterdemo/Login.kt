package com.example.ritwikjha.twitterdemo

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {

    private var mAuth:FirebaseAuth?=null

    private var database=FirebaseDatabase.getInstance()
    private var myRef=database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth=FirebaseAuth.getInstance()

        ivPerson.setOnClickListener{
            checkPermission()
        }
    }
    fun LoginToFirebse(email:String,password:String){

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Successful Login", Toast.LENGTH_SHORT).show()

                        //save in database

                        SaveImageToFirebase()
                    } else {
                        Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                    }

                }
    }

    fun SaveImageToFirebase(){
        var currentUser= mAuth!!.currentUser
        var email= currentUser!!.email.toString()
        val storage=FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitter-8160b.appspot.com")
        val df=SimpleDateFormat("ddMMyyHHmmSS")
        val dateobj=Date()
        val imgpath=SplitString(email)+"."+df.format(dateobj)+".jpg"
        val imgRef=storageRef.child("images/"+imgpath)

        ivPerson.isDrawingCacheEnabled=true
        ivPerson.buildDrawingCache()

        val drawable=ivPerson.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data=baos.toByteArray()
        val uploadTask=imgRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"Failed To Upload",Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            val DownloadURL=taskSnapshot.downloadUrl!!.toString()

            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("Profile Image").setValue(DownloadURL)

            LoadTweets()
        }
    }

    fun SplitString(str:String):String{
        var split=str.split("@")
        return split[0]
    }

    override fun onStart() {
        super.onStart()
        LoadTweets()
    }

    fun LoadTweets(){
        var currentUser=mAuth!!.currentUser

        if (currentUser!=null){
            var intent=Intent(this,MainActivity::class.java)
            intent.putExtra("email",currentUser.email)
            intent.putExtra("uid",currentUser.uid)
            startActivity(intent)
            finish()
        }
    }

    var READIMAGE:Int=253

    fun checkPermission(){
        if (Build.VERSION.SDK_INT>=23){
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)

                return
            }
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){
            READIMAGE->{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }else{
                    Toast.makeText(applicationContext,"Can't Access Your Images",Toast.LENGTH_SHORT).show()
                }
            }
            else->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    val Pick_Image_Code=123

    fun loadImage(){
        var intent=Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,Pick_Image_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==Pick_Image_Code && data!=null && resultCode== Activity.RESULT_OK){
            val selectedImage=data.data
            var filePathCol= arrayOf(MediaStore.Images.Media.DATA)
            var cursor=contentResolver.query(selectedImage,filePathCol,null,null,null)
            cursor.moveToFirst()
            val columnIndex=cursor.getColumnIndex(filePathCol[0])
            val picturePath=cursor.getString(columnIndex)
            cursor.close()

            ivPerson.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    fun buLoginEvent(view: View){
        LoginToFirebse(etEmail.text.toString(),etPassword.text.toString())
    }
}
