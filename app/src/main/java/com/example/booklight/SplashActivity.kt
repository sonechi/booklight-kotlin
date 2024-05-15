package com.example.booklight

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        val postDelayed = Handler().postDelayed(Runnable {
            checkUser()
        }, 1000)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userType = snapshot.child("userType").value
                        if (userType == "user") {
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()
                        }
                        else if (userType == "admin") {
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

    }

}