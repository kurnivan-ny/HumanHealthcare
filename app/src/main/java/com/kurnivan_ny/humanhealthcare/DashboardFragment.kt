package com.kurnivan_ny.humanhealthcare

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kurnivan_ny.humanhealthcare.databinding.FragmentDashboardBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.Konsumsi
import com.kurnivan_ny.humanhealthcare.utils.DashboardViewModel
import com.kurnivan_ny.humanhealthcare.utils.Preferences
import java.util.*
import kotlin.time.measureTime


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
//    lateinit var simpleDataFormat: SimpleDateFormat
    private lateinit var sTanggalMakan:String

    private lateinit var sUsername:String
    private lateinit var sUrl: String

    private lateinit var viewModel: DashboardViewModel

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

        calendar = Calendar.getInstance()
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        sUsername = preferences.getValuesString("username").toString()
        binding.tvNama.text = "Hallo, "+ sUsername

        sUrl = preferences.getValuesString("url").toString()
        storage.reference.child("image_profile/$sUrl").downloadUrl.addOnSuccessListener { Uri ->
            Glide.with(this)
                .load(Uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivProfile)
        }

        if (binding.dateEditText.text.toString().equals("")){
            binding.outputKarbohidrat.text = "0.00 gr"
            binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            binding.outputProtein.text = "0.00 gr"
            binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
            binding.outputLemak.text = "0.00 gr"
            binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
        }

//        simpleDataFormat = SimpleDateFormat("dd MMMM yyyy")
//        sTanggalMakan = simpleDataFormat.format(calendar.time)
//
//        binding.dateEditText.setText(sTanggalMakan)

        getTanggal()

        updateUI()
        binding.btnBreakfast.setOnClickListener {
            if (binding.dateEditText.text.toString().equals("")){
                Toast.makeText(requireContext(), "Silakan Pilih Tanggal Makan", Toast.LENGTH_LONG).show()
                binding.dateEditText.requestFocus()
            } else {
                preferences.setValuesString("waktu_makan", "makan pagi")
                searchMakanan()
            }
        }
        binding.btnLunch.setOnClickListener {
            if (binding.dateEditText.text.toString().equals("")){
                Toast.makeText(requireContext(), "Silakan Pilih Tanggal Makan", Toast.LENGTH_LONG).show()
                binding.dateEditText.requestFocus()
            } else {
                preferences.setValuesString("waktu_makan", "makan siang")
                searchMakanan()
            }
        }
        binding.btnDinner.setOnClickListener {
            if (binding.dateEditText.text.toString().equals("")){
                Toast.makeText(requireContext(), "Silakan Pilih Tanggal Makan", Toast.LENGTH_LONG).show()
                binding.dateEditText.requestFocus()
            } else {
                preferences.setValuesString("waktu_makan", "makan malam")
                searchMakanan()
            }
        }
    }

    private fun searchMakanan() {
        val intent = Intent(activity, ManualActivity::class.java)
        startActivity(intent)
    }

    private fun getTanggal() {

        binding.dateEditText.isFocusable = false


        binding.dateEditText.setOnClickListener {

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->

                    val arrayMonth = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember")

                    sTanggalMakan = (dayOfMonth.toString() + " " + (arrayMonth[monthOfYear]) + " " + year)

                    viewModel.tanggal_makan.value = sTanggalMakan

                    val konsumsi = updateKonsumsi(sTanggalMakan)
                    checkMakan(sUsername,konsumsi)

                }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun updateKonsumsi(sTanggalMakan: String): Konsumsi {
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

                    viewModel.total_karbohidrat_konsumsi.value = (document.get("total_konsumsi_karbohidrat")
                        .toString().replace(",",".")+"F").toFloat()
                    viewModel.total_protein_konsumsi.value = (document.get("total_konsumsi_protein")
                        .toString().replace(",",".")+"F").toFloat()
                    viewModel.total_lemak_konsumsi.value = (document.get("total_konsumsi_lemak")
                        .toString().replace(",",".")+"F").toFloat()
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

        viewModel.total_karbohidrat_konsumsi.value = (data.total_konsumsi_karbohidrat
            .toString().replace(",",".")+"F").toFloat()
        viewModel.total_protein_konsumsi.value = (data.total_konsumsi_protein
            .toString().replace(",",".")+"F").toFloat()
        viewModel.total_lemak_konsumsi.value = (data.total_konsumsi_lemak
            .toString().replace(",",".")+"F").toFloat()
    }


    private fun updateUI() {
        val totalenergikal:Float = preferences.getValuesFloat("totalenergikal")

        viewModel.tanggal_makan.observe(viewLifecycleOwner, androidx.lifecycle.Observer{
            binding.dateEditText.setText(it)
        })

        karbohidrat(totalenergikal)
        protein(totalenergikal)
        lemak(totalenergikal)
    }

    private fun karbohidrat(totalenergikal: Float) {
        // karbohidrat = energi/4 gram
        val batas_bawah_karbohidrat = totalenergikal/4 *0.55
        val batas_atas_karbohidrat = totalenergikal/4 *0.75

        var status_konsumsi_karbohidrat: String

        viewModel.total_karbohidrat_konsumsi.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it < batas_bawah_karbohidrat) {
                binding.outputKarbohidrat.setText("${String.format("%.2f",it)} gr")
                binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
                status_konsumsi_karbohidrat = "Kurang"
                updateKarbohidrattoFirestore(status_konsumsi_karbohidrat)
            } else if  ((it > batas_bawah_karbohidrat)&&(it < batas_atas_karbohidrat)){
                binding.outputKarbohidrat.setText("${String.format("%.2f",it)} gr")
                binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
                status_konsumsi_karbohidrat = "Normal"
                updateKarbohidrattoFirestore(status_konsumsi_karbohidrat)
            } else if (it > batas_atas_karbohidrat) {
                binding.outputKarbohidrat.setText("${String.format("%.2f",it)} gr")
                binding.outputKarbohidrat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
                status_konsumsi_karbohidrat = "Lebih"
                updateKarbohidrattoFirestore(status_konsumsi_karbohidrat)
            }
        })
    }

    private fun updateKarbohidrattoFirestore(status_konsumsi_karbohidrat: String) {
        db.collection("users").document(sUsername)
            .collection("makan").document(preferences.getValuesString("tanggal_makan")!!)
            .update(
                "status_konsumsi_karbohidrat", status_konsumsi_karbohidrat,
            )

        preferences.setValuesString("status_konsumsi_karbohidrat", status_konsumsi_karbohidrat)
    }


    private fun protein(totalenergikal: Float) {
        // protein = energi/4 gram

        val batas_bawah_protein = totalenergikal/4 *0.07
        val batas_atas_protein = totalenergikal/4 *0.2

        var status_konsumsi_protein: String

        viewModel.total_protein_konsumsi.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it < batas_bawah_protein){
                binding.outputProtein.setText("${String.format("%.2f",it)} gr")
                binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
                status_konsumsi_protein = "Kurang"
                updateProteintoFirestore(status_konsumsi_protein)
            } else if  ((it > batas_bawah_protein)&&(it < batas_atas_protein)){
                binding.outputProtein.setText("${String.format("%.2f",it)} gr")
                binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
                status_konsumsi_protein = "Normal"
                updateProteintoFirestore(status_konsumsi_protein)
            } else if (it > batas_atas_protein) {
                binding.outputProtein.setText("${String.format("%.2f",it)} gr")
                binding.outputProtein.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
                status_konsumsi_protein = "Lebih"
                updateProteintoFirestore(status_konsumsi_protein)
            }
        })
    }

    private fun updateProteintoFirestore(status_konsumsi_protein: String) {
        db.collection("users").document(sUsername)
            .collection("makan").document(preferences.getValuesString("tanggal_makan")!!)
            .update(
                "status_konsumsi_protein", status_konsumsi_protein
            )

        preferences.setValuesString("status_konsumsi_protein", status_konsumsi_protein)
    }

    private fun lemak(totalenergikal: Float) {
        // lemak = energi/9 gram

        val batas_bawah_lemak = totalenergikal/9 *0.15
        val batas_atas_lemak = totalenergikal/9 *0.25

        var status_konsumsi_lemak: String

        viewModel.total_lemak_konsumsi.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it < batas_bawah_lemak){
                binding.outputLemak.setText("${String.format("%.2f",it)} gr")
                binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_batas_kurang, 0, 0, 0)
                status_konsumsi_lemak = "Kurang"
                updateLemaktoFirestore(status_konsumsi_lemak)
            } else if  ((it > batas_bawah_lemak)&&(it < batas_atas_lemak)){
                binding.outputLemak.setText("${String.format("%.2f",it)} gr")
                binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.batas_normal, 0, 0, 0)
                status_konsumsi_lemak = "Normal"
                updateLemaktoFirestore(status_konsumsi_lemak)
            } else if (it > batas_atas_lemak) {
                binding.outputLemak.setText("${String.format("%.2f",it)} gr")
                binding.outputLemak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lewat_batas_normal, 0, 0, 0)
                status_konsumsi_lemak = "Lebih"
                updateLemaktoFirestore(status_konsumsi_lemak)
            }
        })
    }

    private fun updateLemaktoFirestore(status_konsumsi_lemak: String) {
        db.collection("users").document(sUsername)
            .collection("makan").document(preferences.getValuesString("tanggal_makan")!!)
            .update(
                "status_konsumsi_lemak", status_konsumsi_lemak
            )

        preferences.setValuesString("status_konsumsi_lemak", status_konsumsi_lemak)

    }
}