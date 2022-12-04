package com.kurnivan_ny.humanhealthcare.sign.signup

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.kurnivan_ny.humanhealthcare.HomeActivity
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.databinding.ActivityDaftarBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.MasukActivity
import com.kurnivan_ny.humanhealthcare.sign.signin.User
import com.kurnivan_ny.humanhealthcare.utils.Preferences


class DaftarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarBinding

    private lateinit var sNama:String
    private lateinit var sJenisKelamin:String
    private lateinit var sUmur:String
    private lateinit var sTinggi:String
    private lateinit var sBerat:String

    private lateinit var sUsername: String
    private lateinit var sEmail: String
    private lateinit var sPassword: String

    private lateinit var mDatabase: DatabaseReference
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDatabase = FirebaseDatabase.getInstance().getReference("User")
        preferences = Preferences(this)

        preferences.setValues("onboarding", "1")

        binding.btnMasuk.setOnClickListener {
            finishAffinity()

            var intent = Intent(this@DaftarActivity, MasukActivity::class.java)
            startActivity(intent)
        }

        setUpForm()
        binding.btnDaftar.setOnClickListener {
            signUpForm()

            if (sNama.equals("")){
                binding.edtNama.error = "Silakan isi Nama Lengkap"
                binding.edtNama.requestFocus()
            } else if (sJenisKelamin.equals("Masukan Jenis Kelamin")){
                binding.edtJenisKelamin.error = "Silakan pilih Jenis Kelamin"
                binding.edtJenisKelamin.requestFocus()
            } else if (sUmur.equals("")){
                binding.edtUmur.error = "Silakan isi Umur"
                binding.edtUmur.requestFocus()
            } else if (sTinggi.equals("")){
                binding.edtTinggi.error = "Silakan isi TInggi Badan (cm)"
                binding.edtTinggi.requestFocus()
            } else if (sBerat.equals("")){
                binding.edtBeratBadan.error = "Silakan isi Berat Badan (kg)"
                binding.edtBeratBadan.requestFocus()
            } else {

                var statusUsername = sUsername.indexOf(".")
                if (statusUsername >= 0){
                    binding.edtUsername.error = "Silakan isi Username tanpa ."
                    binding.edtUsername.requestFocus()
                } else {
                    saveUser(sNama, sJenisKelamin, sUmur, sTinggi, sBerat, sUsername, sEmail, sPassword)
                }
            }
        }

    }

    private fun saveUser(
        sNama: String, sJenisKelamin: String, sUmur: String, sTinggi: String,
        sBerat: String, sUsername: String, sEmail: String, sPassword: String) {
        val user = User()
        user.username = sUsername
        user.email = sEmail
        user.password = sPassword

        user.nama = sNama
        user.jenis_kelamin = sJenisKelamin
        user.umur = sUmur
        user.tinggi = sTinggi
        user.berat = sBerat

        if (sUsername != null){
            checkingUsername(sUsername, user)
        }
    }

    private fun checkingUsername(sUsername: String, data: User) {
        mDatabase.child(sUsername).addValueEventListener(object: ValueEventListener{

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DaftarActivity, error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                var user = snapshot.getValue(data::class.java)
                if (user == null) {
                    savetoFirebase()

                    finishAffinity()
                    val intent = Intent(this@DaftarActivity, HomeActivity::class.java)
                    startActivity(intent)

                } else {
                    Toast.makeText (this@DaftarActivity, "Username sudah digunakan", Toast.LENGTH_LONG)
                }
            }

            private fun savetoFirebase() {
                mDatabase.child(sUsername).setValue(data)

                preferences.setValues("email", data.email.toString())
                preferences.setValues("username", data.username.toString())
                preferences.setValues("password", data.password.toString())
                preferences.setValues("url", "")
                preferences.setValues("nama", data.nama.toString())
                preferences.setValues("jenis_kelamin", data.jenis_kelamin.toString())
                preferences.setValues("umur", data.umur.toString())
                preferences.setValues("tinggi", data.tinggi.toString())
                preferences.setValues("berat", data.berat.toString())
                preferences.setValues("status", "1")
            }
        })
    }

    private fun signUpForm() {
        sNama = binding.edtNama.text.toString()
        sJenisKelamin = binding.edtJenisKelamin.text.toString()
        sUmur = binding.edtUmur.text.toString()
        sTinggi = binding.edtTinggi.text.toString()
        sBerat = binding.edtBeratBadan.text.toString()

        sUsername = binding.edtUsername.text.toString()
        sEmail = binding.edtEmail.text.toString()
        sPassword = binding.edtUmur.text.toString()
    }

    private fun setUpForm() {
        val jeniskelamin = resources.getStringArray(R.array.jenis_kelamin)
        val arrayAdapterJenisKelamin = ArrayAdapter(this, R.layout.dropdown_item, jeniskelamin)
        binding.edtJenisKelamin.setAdapter(arrayAdapterJenisKelamin)
    }
}