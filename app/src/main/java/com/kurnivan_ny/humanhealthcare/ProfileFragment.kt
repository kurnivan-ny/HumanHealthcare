package com.kurnivan_ny.humanhealthcare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.kurnivan_ny.humanhealthcare.R
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kurnivan_ny.humanhealthcare.databinding.FragmentProfileBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.MasukActivity
import com.kurnivan_ny.humanhealthcare.utils.Preferences

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), View.OnClickListener {

    //BINDING
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: Preferences
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

        preferences = Preferences(requireContext())
        db = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()

        //SETUP
        itemOnClickListener()

        showDataUser()
    }

    private fun showDataUser() {

        binding.tvNama.setText(preferences.getValuesString("nama").toString())

        var sUrl = preferences.getValuesString("url").toString()

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
            .setTitle("Apakah Anda yakin ingin keluar?")
            .setView(view)
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
        preferences.clear()
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