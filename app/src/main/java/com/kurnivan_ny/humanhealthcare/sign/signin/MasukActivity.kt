package com.kurnivan_ny.humanhealthcare.sign.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.kurnivan_ny.humanhealthcare.HomeActivity
import com.kurnivan_ny.humanhealthcare.databinding.ActivityMasukBinding
import com.kurnivan_ny.humanhealthcare.sign.signup.DaftarActivity
import com.kurnivan_ny.humanhealthcare.utils.Preferences

class MasukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMasukBinding

    private lateinit var iUsername:String
    private lateinit var iPassword:String

    private lateinit var db: FirebaseFirestore
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        preferences = Preferences(this)

        preferences.setValuesString("onboarding", "1")

        binding.btnDaftar.setOnClickListener {
            var intent = Intent(this@MasukActivity, DaftarActivity::class.java)
            startActivity(intent)
        }

        if(preferences.getValuesString("status").equals("1")){
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
        db.collection("users").document(iUsername).get()
            .addOnSuccessListener{ document ->
                if (document.get("username") == null){
                    Toast.makeText(this@MasukActivity, "Username tidak ditemukan", Toast.LENGTH_LONG).show()
                } else {
                    if (document.get("password")!!.equals(iPassword)){


                        preferences.setValuesString("email", document.get("email").toString())
                        preferences.setValuesString("username", document.get("username").toString())
                        preferences.setValuesString("password", document.get("password").toString())
                        preferences.setValuesString("url", document.get("url").toString())
                        preferences.setValuesString("nama", document.get("nama").toString())
                        preferences.setValuesString("jenis_kelamin", document.get("jenis_kelamin").toString())

                        preferences.setValuesInt("umur", document.get("umur").toString().toInt())
                        preferences.setValuesInt("tinggi", document.get("tinggi").toString().toInt())
                        preferences.setValuesInt("berat", document.get("berat").toString().toInt())

                        preferences.setValuesString("status", "1")

                        finishAffinity()

                        var intent = Intent(this@MasukActivity, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MasukActivity, "Password Anda Salah", Toast.LENGTH_LONG).show()
                    }
                }
            }

    }
}