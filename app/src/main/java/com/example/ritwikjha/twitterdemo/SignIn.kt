package com.example.ritwikjha.twitterdemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignIn : AppCompatActivity() {

    private var mAuth:FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        mAuth=FirebaseAuth.getInstance()

        tvCreate.setOnClickListener {
            var inten=Intent(this,Login::class.java)
            startActivity(inten)
        }
    }

    fun LogIn(){
        LoginToFirebase(etEmail.text.toString(),etPassword.text.toString())
    }

    fun LoginToFirebase(email:String,password:String){
        mAuth!!.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task ->
                    if (task.isSuccessful){

                        Toast.makeText(applicationContext,"Login Successful",Toast.LENGTH_SHORT).show()
                        LoadTweets()


                    }else{
                        Toast.makeText(applicationContext,"Login Failed",Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun LoadTweets(){
        var currentUser=mAuth!!.currentUser

        if (currentUser!=null){

            var intent= Intent(this,MainActivity::class.java)
            intent.putExtra("email",currentUser.email)
            intent.putExtra("uid",currentUser.uid)

            startActivity(intent)
            finish()
        }
    }
}
