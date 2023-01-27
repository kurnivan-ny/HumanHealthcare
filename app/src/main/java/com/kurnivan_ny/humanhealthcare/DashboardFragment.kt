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
    lateinit var date: String
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
        binding.tvNama.setText("Hallo, "+ sUsername)

        sUrl = preferences.getValuesString("url").toString()
        storage.reference.child("image_profile/$sUrl").downloadUrl.addOnSuccessListener { Uri ->
            Glide.with(this)
                .load(Uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivProfile)
        }

        calendar = Calendar.getInstance()
        getCalendar()

        val totalenergikal:Float = preferences.getValuesFloat("totalenergikal")

        AMDR(totalenergikal, sUsername, STanggalMakan)

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
        calendar = Calendar.getInstance()
        simpleDataFormat = SimpleDateFormat("dd-M-yyyy")
        STanggalMakan = simpleDataFormat.format(calendar.time)

        binding.dateEditText.isFocusable = false

        binding.dateEditText.setText(STanggalMakan)

        binding.dateEditText.setOnClickListener {

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->


                    STanggalMakan = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    binding.dateEditText.setText(STanggalMakan)
                }, year, month, day
            )
            datePickerDialog.show()

            updateMakan(sUsername, STanggalMakan)
        }
    }

    private fun updateMakan(sUsername: String, sTanggalMakan: String) {
        val konsumsi = Konsumsi()
        konsumsi.tanggal_makan = sTanggalMakan

        konsumsi.total_konsumsi_kabohidrat = 0.00F
        konsumsi.total_konsumsi_protein = 0.00F
        konsumsi.total_konsumsi_lemak = 0.00F

        konsumsi.status_konsumsi_karbohidrat = Status_konsumsi_karbohidrat
        konsumsi.status_konsumsi_protein = Status_konsumsi_protein
        konsumsi.status_konsumsi_lemak = Status_konsumsi_lemak

        checkMakan(sUsername, konsumsi)
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

        preferences.setValuesFloat("total_konsumsi_karbohidrat", (data.total_konsumsi_kabohidrat
            .toString().replace(",",".")+"F").toFloat())
        preferences.setValuesFloat("total_konsumsi_protein", (data.total_konsumsi_protein
            .toString().replace(",",".")+"F").toFloat())
        preferences.setValuesFloat("total_konsumsi_lemak", (data.total_konsumsi_lemak
            .toString().replace(",",".")+"F").toFloat())

        preferences.setValuesString("status_konsumsi_karbohidrat", data.status_konsumsi_karbohidrat.toString())
        preferences.setValuesString("status_konsumsi_protein", data.status_konsumsi_protein.toString())
        preferences.setValuesString("status_konsumsi_lemak", data.status_konsumsi_lemak.toString())
    }


    private fun AMDR(totalenergikal: Float, sUsername: String, sTanggalMakan: String) {
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
        var total_karbohidrat:Float= preferences.getValuesFloat("total_konsumsi_lemak")/4
//        var total_karbohidrat =500
        val total_karbohidrat_kal = total_karbohidrat

        val batas_bawah_karbohidrat = totalenergikal*0.55
        val batas_atas_karbohidrat = totalenergikal*0.75

        if (total_karbohidrat_kal < batas_bawah_karbohidrat) {
            binding.outputKarbohidrat.text = "${total_karbohidrat_kal} gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            binding.outputKarbohidrat.setTextColor(Color.YELLOW)
            Status_konsumsi_karbohidrat = "Kurang"
        } else if  ((total_karbohidrat_kal > batas_bawah_karbohidrat)&&(total_karbohidrat_kal < batas_atas_karbohidrat)){
            binding.outputKarbohidrat.text = "${total_karbohidrat_kal} gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
            binding.outputKarbohidrat.setTextColor(Color.GREEN)
            Status_konsumsi_karbohidrat = "Normal"
        } else if (total_karbohidrat_kal > batas_atas_karbohidrat) {
            binding.outputKarbohidrat.text = "${total_karbohidrat_kal} gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
            binding.outputKarbohidrat.setTextColor(Color.RED)
            Status_konsumsi_karbohidrat = "Lebih"
        }
    }

    private fun protein(totalenergikal: Float) {
        // protein = energi/4 gram
        val total_protein:Float = preferences.getValuesFloat("total_konsumsi_lemak")/4
//        val total_protein =500
        val total_protein_kal = total_protein

        val batas_bawah_protein = totalenergikal*0.07
        val batas_atas_protein = totalenergikal*0.2

        if (total_protein_kal < batas_bawah_protein){
            binding.outputProtein.text = "${total_protein_kal} gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            binding.outputProtein.setTextColor(Color.YELLOW)
            Status_konsumsi_protein = "Kurang"
        } else if  ((total_protein_kal > batas_bawah_protein)&&(total_protein_kal < batas_atas_protein)){
            binding.outputProtein.text = "${total_protein_kal} gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
            binding.outputProtein.setTextColor(Color.GREEN)
            Status_konsumsi_protein = "Normal"
        } else if (total_protein_kal > batas_atas_protein) {
            binding.outputProtein.text = "${total_protein_kal} gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
            binding.outputProtein.setTextColor(Color.RED)
            Status_konsumsi_protein = "Lebih"
        }
    }

    private fun lemak(totalenergikal: Float) {
        // lemak = energi/9 gram
        val total_lemak:Float = preferences.getValuesFloat("total_konsumsi_lemak")/9

//        val total_lemak = 500
        val total_lemak_kal = total_lemak

        val batas_bawah_lemak = totalenergikal*0.15
        val batas_atas_lemak = totalenergikal*0.25

        if (total_lemak_kal < batas_bawah_lemak){
            binding.outputLemak.text = "${total_lemak_kal} gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            binding.outputLemak.setTextColor(Color.YELLOW)
            Status_konsumsi_lemak = "Kurang"
        } else if  ((total_lemak_kal > batas_bawah_lemak)&&(total_lemak_kal < batas_atas_lemak)){
            binding.outputLemak.text = "${total_lemak_kal} gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
            binding.outputLemak.setTextColor(Color.GREEN)
            Status_konsumsi_lemak = "Normal"
        } else if (total_lemak_kal > batas_atas_lemak) {
            binding.outputLemak.text = "${total_lemak_kal} gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
            binding.outputLemak.setTextColor(Color.RED)
            Status_konsumsi_lemak = "Lebih"
        }
    }
}