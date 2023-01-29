package com.kurnivan_ny.humanhealthcare

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kurnivan_ny.humanhealthcare.databinding.FragmentDashboardBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.Konsumsi
import com.kurnivan_ny.humanhealthcare.sign.signin.Makan
import com.kurnivan_ny.humanhealthcare.utils.Preferences
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class DashboardFragment : Fragment() {

    //BINDING
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: Preferences
    lateinit var storage: FirebaseStorage
    lateinit var db: FirebaseFirestore

    lateinit var calendar: Calendar
    lateinit var simpleDataFormat: SimpleDateFormat
    private lateinit var STanggalMakan:String

    private lateinit var sUsername:String
    private lateinit var sUrl: String

    private lateinit var Status_konsumsi_karbohidrat: String
    private lateinit var Status_konsumsi_protein: String
    private lateinit var Status_konsumsi_lemak: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = Preferences(requireContext())
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        sUsername = preferences.getValuesString("username").toString()
        binding.tvNama.text = "Hallo, "+ sUsername

        sUrl = preferences.getValuesString("url").toString()
        storage.reference.child("image_profile/$sUrl").downloadUrl.addOnSuccessListener { Uri ->
            Glide.with(this)
                .load(Uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivProfile)
        }

        calendar = Calendar.getInstance()
        simpleDataFormat = SimpleDateFormat("dd MMMM yyyy")
        STanggalMakan = simpleDataFormat.format(calendar.time)

        binding.dateEditText.setText(STanggalMakan)

        getCalendar()

        AMDR(sUsername, STanggalMakan)

        binding.btnBreakfast.setOnClickListener {
            preferences.setValuesString("waktu_makan", "Makan Pagi")
            searchMakanan()
        }
        binding.btnLunch.setOnClickListener {
            preferences.setValuesString("waktu_makan", "Makan Siang")
            searchMakanan()
        }
        binding.btnDinner.setOnClickListener {
            preferences.setValuesString("waktu_makan", "Makan Malam")
            searchMakanan()
        }
    }

    private fun searchMakanan() {
        val intent = Intent(activity, ManualActivity::class.java)
        startActivity(intent)
    }

    private fun getCalendar() {

        binding.dateEditText.isFocusable = false


        binding.dateEditText.setOnClickListener {

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->

                    val arrayMonth = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember")
                    STanggalMakan = (dayOfMonth.toString() + " " + (arrayMonth[monthOfYear]) + " " + year)
                    binding.dateEditText.setText(STanggalMakan)

                    val konsumsi = updatedataMakan(STanggalMakan)
                    checkMakan(sUsername,konsumsi)
                }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun updatedataMakan(sTanggalMakan: String): Konsumsi {
        val konsumsi = Konsumsi()
        konsumsi.tanggal_makan = sTanggalMakan

        konsumsi.total_konsumsi_karbohidrat = 0.00F
        konsumsi.total_konsumsi_protein = 0.00F
        konsumsi.total_konsumsi_lemak = 0.00F

        konsumsi.status_konsumsi_karbohidrat = "Kurang"
        konsumsi.status_konsumsi_protein = "Kurang"
        konsumsi.status_konsumsi_lemak = "Kurang"

        return konsumsi
    }

    private fun checkMakan(sUsername: String, data: Konsumsi) {
        db.collection("users").document(sUsername)
            .collection("makan").document(data.tanggal_makan!!).get()
            .addOnSuccessListener { document ->
                if (document.get("tanggal_makan") == null) {
                    savetoFirestore(sUsername, data)
                }
                else {
                    // Get Values From Firestore
                    preferences.setValuesString("tanggal_makan", document.get("tanggal_makan").toString())

                    preferences.setValuesFloat("total_konsumsi_karbohidrat", (document.get("total_konsumsi_karbohidrat")
                        .toString().replace(",",".")+"F").toFloat())
                    preferences.setValuesFloat("total_konsumsi_protein", (document.get("total_konsumsi_protein")
                        .toString().replace(",",".")+"F").toFloat())
                    preferences.setValuesFloat("total_konsumsi_lemak", (document.get("total_konsumsi_lemak")
                        .toString().replace(",",".")+"F").toFloat())

                    preferences.setValuesString("status_konsumsi_karbohidrat", document.get("status_konsumsi_karbohidrat").toString())
                    preferences.setValuesString("status_konsumsi_protein", document.get("status_konsumsi_protein").toString())
                    preferences.setValuesString("status_konsumsi_lemak", document.get("status_konsumsi_lemak").toString())
                }
            }
    }

    private fun savetoFirestore(sUsername: String, data: Konsumsi) {
        db.collection("users").document(sUsername)
            .collection("makan").document(data.tanggal_makan!!)
            .set(data)

        preferences.setValuesString("tanggal_makan", data.tanggal_makan.toString())

        preferences.setValuesFloat("total_konsumsi_karbohidrat", (data.total_konsumsi_karbohidrat
            .toString().replace(",",".")+"F").toFloat())
        preferences.setValuesFloat("total_konsumsi_protein", (data.total_konsumsi_protein
            .toString().replace(",",".")+"F").toFloat())
        preferences.setValuesFloat("total_konsumsi_lemak", (data.total_konsumsi_lemak
            .toString().replace(",",".")+"F").toFloat())

        preferences.setValuesString("status_konsumsi_karbohidrat", data.status_konsumsi_karbohidrat.toString())
        preferences.setValuesString("status_konsumsi_protein", data.status_konsumsi_protein.toString())
        preferences.setValuesString("status_konsumsi_lemak", data.status_konsumsi_lemak.toString())
    }


    private fun AMDR(sUsername: String, sTanggalMakan: String) {
        val totalenergikal:Float = preferences.getValuesFloat("totalenergikal")
        karbohidrat(totalenergikal)
        protein(totalenergikal)
        lemak(totalenergikal)

        db.collection("users").document(sUsername)
            .collection("makan").document(sTanggalMakan)
            .update(
                "status_konsumsi_karbohidrat", Status_konsumsi_karbohidrat,
                "status_konsumsi_protein", Status_konsumsi_protein,
                "status_konsumsi_lemak", Status_konsumsi_lemak
            )

        preferences.setValuesString("status_konsumsi_karbohidrat", Status_konsumsi_karbohidrat)
        preferences.setValuesString("status_konsumsi_protein", Status_konsumsi_protein)
        preferences.setValuesString("status_konsumsi_lemak", Status_konsumsi_lemak)
    }

    private fun karbohidrat(totalenergikal: Float) {
        // karbohidrat = energi/4 gram
        var total_karbohidrat:Float= preferences.getValuesFloat("total_konsumsi_karbohidrat")

        val batas_bawah_karbohidrat = totalenergikal/4 *0.55
        val batas_atas_karbohidrat = totalenergikal/4 *0.75

        if (total_karbohidrat < batas_bawah_karbohidrat) {
            binding.outputKarbohidrat.text = "${String.format("%.2f",total_karbohidrat)} gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            Status_konsumsi_karbohidrat = "Kurang"
        } else if  ((total_karbohidrat > batas_bawah_karbohidrat)&&(total_karbohidrat < batas_atas_karbohidrat)){
            binding.outputKarbohidrat.text = "${String.format("%.2f",total_karbohidrat)} gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
            Status_konsumsi_karbohidrat = "Normal"
        } else if (total_karbohidrat > batas_atas_karbohidrat) {
            binding.outputKarbohidrat.text = "${String.format("%.2f",total_karbohidrat)} gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
            Status_konsumsi_karbohidrat = "Lebih"
        }
    }

    private fun protein(totalenergikal: Float) {
        // protein = energi/4 gram
        val total_protein:Float = preferences.getValuesFloat("total_konsumsi_protein")

        val batas_bawah_protein = totalenergikal/4 *0.07
        val batas_atas_protein = totalenergikal/4 *0.2

        if (total_protein < batas_bawah_protein){
            binding.outputProtein.text = "${String.format("%.2f",total_protein)} gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            Status_konsumsi_protein = "Kurang"
        } else if  ((total_protein > batas_bawah_protein)&&(total_protein < batas_atas_protein)){
            binding.outputProtein.text = "${String.format("%.2f",total_protein)} gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
            Status_konsumsi_protein = "Normal"
        } else if (total_protein > batas_atas_protein) {
            binding.outputProtein.text = "${String.format("%.2f",total_protein)} gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
            Status_konsumsi_protein = "Lebih"
        }
    }

    private fun lemak(totalenergikal: Float) {
        // lemak = energi/9 gram
        val total_lemak:Float = preferences.getValuesFloat("total_konsumsi_lemak")

        val batas_bawah_lemak = totalenergikal/9 *0.15
        val batas_atas_lemak = totalenergikal/9 *0.25

        if (total_lemak < batas_bawah_lemak){
            binding.outputLemak.text = "${String.format("%.2f",total_lemak)} gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            Status_konsumsi_lemak = "Kurang"
        } else if  ((total_lemak > batas_bawah_lemak)&&(total_lemak < batas_atas_lemak)){
            binding.outputLemak.text = "${String.format("%.2f",total_lemak)} gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
            Status_konsumsi_lemak = "Normal"
        } else if (total_lemak > batas_atas_lemak) {
            binding.outputLemak.text = "${String.format("%.2f",total_lemak)} gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
            Status_konsumsi_lemak = "Lebih"
        }
    }
}