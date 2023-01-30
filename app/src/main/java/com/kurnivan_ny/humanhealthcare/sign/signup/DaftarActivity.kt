package com.kurnivan_ny.humanhealthcare.sign.signup

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.kurnivan_ny.humanhealthcare.HomeActivity
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.databinding.ActivityDaftarBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.MasukActivity
import com.kurnivan_ny.humanhealthcare.sign.signin.User
import com.kurnivan_ny.humanhealthcare.utils.Preferences
import java.nio.FloatBuffer


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

    private lateinit var db: FirebaseFirestore
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        preferences = Preferences(this)

        preferences.setValuesString("onboarding", "1")

        binding.btnMasuk.setOnClickListener {
            finishAffinity()

            val intent = Intent(this@DaftarActivity, MasukActivity::class.java)
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
                    var TotalEnergi:Float = predictRegressi(sJenisKelamin, sUmur, sTinggi, sBerat)
                    saveUser(sNama, sJenisKelamin, sUmur, sTinggi, sBerat, sUsername, sEmail, sPassword, TotalEnergi)
                }
            }
        }

    }

    private fun saveUser(
        sNama: String, sJenisKelamin: String, sUmur: String, sTinggi: String,
        sBerat: String, sUsername: String, sEmail: String, sPassword: String,
        TotalEnergi: Float) {

        val user = User()
        user.username = sUsername
        user.email = sEmail
        user.password = sPassword

        user.nama = sNama
        user.jenis_kelamin = sJenisKelamin

        user.umur = sUmur.toInt()
        user.tinggi = sTinggi.toInt()
        user.berat = sBerat.toInt()

        user.url = "default.png"

        user.totalenergikal = TotalEnergi

        if (sUsername != null){
            checkingUsername(sUsername, user)
        }
    }

    private fun checkingUsername(sUsername: String, data: User) {
        db.collection("users").document(sUsername).get()
            .addOnSuccessListener { document ->
                if (document.get("username") == null) {
                    savetoFirestore(data)
                    finishAffinity()
                    val intent = Intent(this@DaftarActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText (this, "Username sudah digunakan", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText (this, exception.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun savetoFirestore(data: User) {
        db.collection("users")
            .document(sUsername)
            .set(data)

        preferences.setValuesString("email", data.email.toString())
        preferences.setValuesString("username", data.username.toString())
        preferences.setValuesString("password", data.password.toString())

        preferences.setValuesString("nama", data.nama.toString())
        preferences.setValuesString("url", data.url.toString())
        preferences.setValuesString("jenis_kelamin", data.jenis_kelamin.toString())

        preferences.setValuesInt("umur", data.umur.toString().toInt())
        preferences.setValuesInt("tinggi", data.tinggi.toString().toInt())
        preferences.setValuesInt("berat", data.berat.toString().toInt())
        preferences.setValuesFloat("totalenergikal", data.totalenergikal.toString().toFloat())

        preferences.setValuesString("status", "1")
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

    private fun createORTSession(ortEnvironment: OrtEnvironment): OrtSession {

        val modelBytes = resources.openRawResource(R.raw.sklearn_model).readBytes()
        return ortEnvironment.createSession(modelBytes)
    }

    private fun runPrediction(
        floatBufferInputs: FloatBuffer?,
        ortSession: OrtSession,
        ortEnvironment: OrtEnvironment?
    ): Any {
        val inputName = ortSession.inputNames?.iterator()?.next()
        val inputTensor =
            OnnxTensor.createTensor(ortEnvironment, floatBufferInputs, longArrayOf(1, 5))
        val results = ortSession.run(mapOf(inputName to inputTensor))
        val output = results[0].value as Array<FloatArray>
        return output[0][0]
    }

    private fun predictRegressi(sJenisKelamin: String, sUmur: String, sTinggi: String, sBerat: String): Float {

        var TotalEnergi:Float = if (sJenisKelamin.equals("Laki-laki")){
            val floatBufferInputs = FloatBuffer.wrap(
                floatArrayOf(
                    sUmur.toFloat(),
                    sBerat.toFloat(),
                    sTinggi.toFloat(),
                    1.0f,
                    0.0f
                )
            )
            outputPrediction(floatBufferInputs)
        } else {
            val floatBufferInputs = FloatBuffer.wrap(
                floatArrayOf(
                    sUmur.toFloat(),
                    sBerat.toFloat(),
                    sTinggi.toFloat(),
                    0.0f,
                    1.0f
                )
            )
            outputPrediction(floatBufferInputs)
        }
        return TotalEnergi
    }

    private fun outputPrediction(floatBufferInputs: FloatBuffer?): Float {
        val ortEnvironment = OrtEnvironment.getEnvironment()
        val ortSession = createORTSession(ortEnvironment)
        val output = runPrediction(floatBufferInputs, ortSession, ortEnvironment)

        var TotalEnergi:Float = (String.format("%.2f", output).replace(",",".") + "F").toFloat()

        return TotalEnergi

    }
}