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
import com.kurnivan_ny.humanhealthcare.databinding.FragmentProfileBinding
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
    lateinit var mDatabase: DatabaseReference

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
        mDatabase = FirebaseDatabase.getInstance().getReference("User")

        //SETUP
        itemOnClickListener()

        showDataUser()
    }

    private fun showDataUser() {

        binding.tvNama.setText(preferences.getValues("nama").toString())

        Glide.with(this)
            .load(preferences.getValues("url"))
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivProfile)
    }


    private fun itemOnClickListener(){
        binding.btnEdtProfile.setOnClickListener(this)
//        binding.btnPengAkun.setOnClickListener(this)
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
                //alertLogout()
            }
        }
    }

    private fun killActivity() {
        activity?.finish()
    }

}