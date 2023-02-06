package com.kurnivan_ny.humanhealthcare.ui.main.manualinput

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.databinding.ActivityDetailMakananBinding
import com.kurnivan_ny.humanhealthcare.data.pushFirestore.Makan
import com.kurnivan_ny.humanhealthcare.viewmodel.preferences.SharedPreferences


class DetailMakananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailMakananBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

    private lateinit var satuan: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMakananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = SharedPreferences(this)
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val nama_makanan = intent.getStringExtra("nama_makanan").toString()

        getDataFirestore(nama_makanan)

    }

    private fun getDataFirestore(namaMakanan: String) {
        db.collection("food").document(namaMakanan).get().addOnSuccessListener { document ->

            val nama_makanan = document.get("nama_makanan").toString()

            // setting image
            val img_url = document.get("url_foto").toString()

            var karbohidrat = (document.get("karbohidrat_100gr").toString() + "F").toFloat()
            var protein = (document.get("protein_100gr").toString() + "F").toFloat()
            var lemak = (document.get("lemak_100gr").toString() + "F").toFloat()

            setUpForm(nama_makanan, img_url, karbohidrat, protein, lemak)
        }
    }

    private fun setUpForm(namaMakanan: String, imgUrl: String, karbohidrat: Float, protein: Float, lemak: Float) {
        binding.edtNamaMakanan.text = namaMakanan
        storage.reference.child("image_profile/$imgUrl").downloadUrl.addOnSuccessListener { Uri ->
            Glide.with(this)
                .load(Uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivMakanan)
        }

        val satuan_makanan = resources.getStringArray(R.array.satuan)
        val arrayAdapterSatuan = ArrayAdapter(this, R.layout.dropdown_item, satuan_makanan)
        binding.edtSatuanMakanan.setText("hidangan")
        binding.edtSatuanMakanan.setAdapter(arrayAdapterSatuan)

        binding.btnTambah.isEnabled = false
        binding.btnTambah.visibility = View.INVISIBLE

        binding.edtBeratMakanan.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                satuan = binding.edtSatuanMakanan.text.toString()

                var berat_makanan = binding.edtBeratMakanan.text.toString()

                if (berat_makanan.equals("") or berat_makanan.equals("0")){

                    binding.tvKarbohidrat.text = "Karbohidrat:\t 0.00 gr"
                    binding.tvProtein.text = "Protein:\t\t\t\t\t\t 0.00 gr"
                    binding.tvLemak.text = "Lemak:\t\t\t\t\t\t\t 0.00 gr"

                    binding.btnTambah.isEnabled = false
                    binding.btnTambah.visibility = View.INVISIBLE
                } else {
                    var beratMakanan = berat_makanan.toInt()
                    val arrayTotal = hitungKebutuhan(beratMakanan, karbohidrat, protein, lemak)

                    binding.btnTambah.isEnabled = true
                    binding.btnTambah.visibility = View.VISIBLE

                    binding.btnTambah.setOnClickListener {

                    saveMakanantoFirestore(namaMakanan, satuan, beratMakanan, arrayTotal)

                        val intent = Intent(this@DetailMakananActivity, ManualActivity::class.java)
                        startActivity(intent)
                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun saveMakanantoFirestore(namaMakanan: String, satuan: String, beratMakanan: Int, arrayTotal: FloatArray) {
        val username = sharedPreferences.getValuesString("username")
        val tanggal_makan = sharedPreferences.getValuesString("tanggal_makan")
        val waktu_makan = sharedPreferences.getValuesString("waktu_makan")
        val bulan_makan = sharedPreferences.getValuesString("bulan_makan")

        val makan = Makan()
        makan.waktu_makan = waktu_makan

        makan.nama_makanan = namaMakanan
        makan.satuan_makanan = satuan
        makan. berat_makanan = beratMakanan

        makan.karbohidrat = arrayTotal[0]
        makan.protein = arrayTotal[1]
        makan.lemak = arrayTotal[2]

        db.collection("users").document(username!!)
            .collection(bulan_makan!!).document(tanggal_makan!!)
            .collection(waktu_makan!!).document(namaMakanan).set(makan)


        // update value username

        db.collection("users").document(username!!)
            .collection(bulan_makan!!).document(tanggal_makan!!)
            .get().addOnSuccessListener {
                var total_karbohidrat:Float = (it.get("total_konsumsi_karbohidrat").toString()+"F").toFloat()
                var total_protein:Float = (it.get("total_konsumsi_protein").toString()+"F").toFloat()
                var total_lemak:Float = (it.get("total_konsumsi_lemak").toString()+"F").toFloat()

                total_karbohidrat += arrayTotal[0]
                total_protein += arrayTotal[1]
                total_lemak += arrayTotal[2]

                db.collection("users").document(username!!)
                    .collection(bulan_makan!!).document(tanggal_makan!!)
                    .update("total_konsumsi_karbohidrat",total_karbohidrat,
                    "total_konsumsi_protein", total_protein,
                    "total_konsumsi_lemak", total_lemak)
            }
//        var total_karbohidrat = sharedPreferences.getValuesFloat("total_konsumsi_karbohidrat")
//        var total_protein = sharedPreferences.getValuesFloat("total_konsumsi_protein")
//        var total_lemak = sharedPreferences.getValuesFloat("total_konsumsi_lemak")
//
//        total_karbohidrat += arrayTotal[0]
//        total_protein += arrayTotal[1]
//        total_lemak += arrayTotal[2]
//
//        db.collection("users").document(username!!)
//            .collection("makan").document(tanggal_makan!!)
//            .update("total_konsumsi_karbohidrat",total_karbohidrat,
//            "total_konsumsi_protein", total_protein,
//            "total_konsumsi_lemak", total_lemak)
//
//        sharedPreferences.setValuesFloat("total_konsumsi_karbohidrat", total_karbohidrat)
//        sharedPreferences.setValuesFloat("total_konsumsi_protein", total_protein)
//        sharedPreferences.setValuesFloat("total_konsumsi_lemak",total_lemak)

    }

    private fun hitungKebutuhan(beratMakanan: Int, karbohidrat: Float, protein: Float, lemak: Float): FloatArray {

        var total_karbohidrat:Float? = null
        var total_protein:Float? = null
        var total_lemak: Float? = null

        if (satuan.equals("hidangan")){
            var berat_hidangan_gr = 100 // edit

            total_karbohidrat = karbohidrat * berat_hidangan_gr * beratMakanan / 100
            total_protein = protein * berat_hidangan_gr * beratMakanan / 100
            total_lemak = lemak * berat_hidangan_gr * beratMakanan / 100

            binding.tvKarbohidrat.text = "Karbohidrat:\t ${String.format("%.2f",total_karbohidrat)} gr"
            binding.tvProtein.text = "Protein:\t\t\t\t\t\t ${String.format("%.2f",total_protein)} gr"
            binding.tvLemak.text = "Lemak:\t\t\t\t\t\t\t ${String.format("%.2f",total_lemak)} gr"
        }
        else if (satuan.equals("gram")){

            total_karbohidrat = karbohidrat * beratMakanan / 100
            total_protein = protein * beratMakanan / 100
            total_lemak = lemak * beratMakanan / 100

            binding.tvKarbohidrat.text = "Karbohidrat:\t ${String.format("%.2f",total_karbohidrat)} gr"
            binding.tvProtein.text = "Protein:\t\t\t\t\t\t ${String.format("%.2f",total_protein)} gr"
            binding.tvLemak.text = "Lemak:\t\t\t\t\t\t\t ${String.format("%.2f",total_lemak)} gr"
        }

        val arrayTotal = floatArrayOf(total_karbohidrat!!,total_protein!!,total_lemak!!)

        return arrayTotal
    }
}