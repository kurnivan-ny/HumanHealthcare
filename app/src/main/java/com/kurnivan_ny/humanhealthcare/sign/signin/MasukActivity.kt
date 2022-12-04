package com.kurnivan_ny.humanhealthcare.sign.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import com.kurnivan_ny.humanhealthcare.HomeActivity
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.databinding.ActivityMasukBinding
import com.kurnivan_ny.humanhealthcare.sign.signup.DaftarActivity
import com.kurnivan_ny.humanhealthcare.utils.Preferences

class MasukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMasukBinding

    private lateinit var iUsername:String
    private lateinit var iPassword:String

    private lateinit var mDatabase: DatabaseReference
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDatabase = FirebaseDatabase.getInstance().getReference("User")
        preferences = Preferences(this)

        preferences.setValues("onboarding", "1")

        binding.btnDaftar.setOnClickListener {
            var intent = Intent(this@MasukActivity, DaftarActivity::class.java)
            startActivity(intent)
        }

        if(preferences.getValues("status").equals("1")){
            finishAffinity()

            var goHome = Intent(this@MasukActivity, HomeActivity::class.java)
            startActivity(goHome)
        }
        
        binding.btnMasuk.setOnClickListener {
            iUsername = binding.etUsername.text.toString()
            iPassword = binding.etPassword.text.toString()
            
            if(iUsername.equals("")){
                binding.etUsername.error = "Silakan tulis username Anda"
                binding.etUsername.requestFocus()
            } else if(iPassword.equals("")){
                binding.etPassword.error = "Silakan tulis password Anda"
                binding.etPassword.requestFocus()
            } else{
                pushLogin(iUsername, iPassword)
            }
        }
    }

    private fun pushLogin(iUsername: String, iPassword: String) {
        mDatabase.child(iUsername).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MasukActivity, error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var user = snapshot.getValue(User::class.java)
                if (user == null){
                    Toast.makeText(this@MasukActivity, "Username tidak ditemukan", Toast.LENGTH_LONG).show()
                } else {

                    if (user.password.equals(iPassword)){
                        preferences.setValues("email", user.email.toString())
                        preferences.setValues("username", user.username.toString())
                        preferences.setValues("password", user.password.toString())
                        preferences.setValues("url", user.url.toString())
                        preferences.setValues("nama", user.nama.toString())
                        preferences.setValues("jenis_kelamin", user.jenis_kelamin.toString())
                        preferences.setValues("umur", user.umur.toString())
                        preferences.setValues("tinggi", user.tinggi.toString())
                        preferences.setValues("berat", user.berat.toString())
                        preferences.setValues("status", "1")

                        finishAffinity()

                        var intent = Intent(this@MasukActivity, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MasukActivity, "Password Anda Salah", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

    }
}