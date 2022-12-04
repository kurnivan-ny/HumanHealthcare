package com.kurnivan_ny.humanhealthcare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kurnivan_ny.humanhealthcare.databinding.FragmentDashboardBinding
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
    lateinit var mDatabase: DatabaseReference

    lateinit var calendar: Calendar
    lateinit var simpleDataFormat: SimpleDateFormat
    lateinit var date: String

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
        mDatabase = FirebaseDatabase.getInstance().getReference("User")

        binding.tvNama.setText("Hallo, "+preferences.getValues("username").toString())

        Glide.with(this)
            .load(preferences.getValues("url"))
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivProfile)

        calendar = Calendar.getInstance()
        simpleDataFormat = SimpleDateFormat("dd MMMM yyyy")
        date = simpleDataFormat.format(calendar.time)

        binding.dateEditText.setText(date)

        //binding.btnBreakfast.setOnClickListener {  }
    }

}