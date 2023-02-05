package com.kurnivan_ny.humanhealthcare.ui.main.fragment.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.databinding.FragmentProfileBinding
import com.kurnivan_ny.humanhealthcare.ui.sign.MasukActivity
import com.kurnivan_ny.humanhealthcare.viewmodel.preferences.SharedPreferences

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), View.OnClickListener {

    //BINDING
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var storage: FirebaseStorage
    lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = SharedPreferences(requireContext())
        db = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()

        //SETUP
        itemOnClickListener()

        showDataUser()
    }

    private fun showDataUser() {

        binding.tvNama.setText(sharedPreferences.getValuesString("nama").toString())

        var sUrl = sharedPreferences.getValuesString("url").toString()

        storage.reference.child("image_profile/$sUrl").downloadUrl.addOnSuccessListener { Uri ->
            Glide.with(this)
                .load(Uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivProfile)
        }
    }


    private fun itemOnClickListener(){
        binding.btnEdtProfile.setOnClickListener(this)
        binding.btnKeluar.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_edt_profile -> {
                val toEditProfileFragment = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
                binding.root.findNavController().navigate(toEditProfileFragment)
            }
            R.id.btn_keluar -> {

                //logout, go to login activity
                alertLogout()
            }
        }
    }

    private fun alertLogout() {
        val view = View.inflate(requireContext(), R.layout.profile_logout_dialog, null)

        AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme)
            .setView(view)
            .setCancelable(false)
            .setNegativeButton("Tidak"){ p0, _ ->
                p0.dismiss()
            }
            .setPositiveButton("Ya"){_, _ ->
                progressBar(true)
                observerLogout()
            }.show()
    }

    private fun observerLogout() {
        progressBar(false)
        sharedPreferences.clear()
        killActivity()
        startActivity(Intent(requireContext(), MasukActivity::class.java))
    }

    private fun killActivity() {
        activity?.finish()
    }

    private fun progressBar(isLoading: Boolean) = with(binding){
        if (isLoading){
            this.progressBar.visibility = View.VISIBLE
        } else {
            this.progressBar.visibility = View.GONE
        }
    }
}