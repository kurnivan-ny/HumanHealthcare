package com.kurnivan_ny.humanhealthcare

import android.app.ProgressDialog
import android.content.Context
import android.icu.number.NumberRangeFormatter.with
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kurnivan_ny.humanhealthcare.databinding.FragmentEditProfileBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.User
import com.kurnivan_ny.humanhealthcare.utils.Preferences
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {

    //BINDING
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: Preferences
    lateinit var mDatabase: DatabaseReference
    lateinit var mInstance: FirebaseDatabase

    private lateinit var sUsername: String
    private lateinit var sNama:String
    private lateinit var sJenisKelamin:String
    private lateinit var sUmur:String
    private lateinit var sTinggi:String
    private lateinit var sBerat:String

    val REQUEST_IMAGE_CAPTURE = 1
    var statusAdd:Boolean = false
    lateinit var filePath: Uri

    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
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

        setUpFrom()

        //edtFotoProfile()

        itemOnClickListener()
    }

//    private fun edtFotoProfile() {
//        binding.tvAdd.setOnClickListener {
//            ImagePicker.with(this)
//                .cameraOnly()
//                .start()
//        }
//    }

    private fun itemOnClickListener() {
        binding.ivBack.setOnClickListener {
            val toProfileFragment =
                EditProfileFragmentDirections.actionEditProfileFragmentToProfileFragment()
            binding.root.findNavController().navigate(toProfileFragment)
        }

        binding.btnSimpan.setOnClickListener {
            edtForm()
            if (sNama.equals("")) {
                binding.edtNama.error = "Silakan isi Nama Lengkap"
                binding.edtNama.requestFocus()
            } else if (sJenisKelamin.equals("Masukan Jenis Kelamin")) {
                binding.edtJenisKelamin.error = "Silakan pilih Jenis Kelamin"
                binding.edtJenisKelamin.requestFocus()
            } else if (sUmur.equals("")) {
                binding.edtUmur.error = "Silakan isi Umur"
                binding.edtUmur.requestFocus()
            } else if (sTinggi.equals("")) {
                binding.edtTinggi.error = "Silakan isi TInggi Badan (cm)"
                binding.edtTinggi.requestFocus()
            } else if (sBerat.equals("")) {
                binding.edtBeratBadan.error = "Silakan isi Berat Badan (kg)"
                binding.edtBeratBadan.requestFocus()
            } else {
                saveUser(sNama, sJenisKelamin, sUmur, sTinggi, sBerat)
                val toProfileFragment =
                    EditProfileFragmentDirections.actionEditProfileFragmentToProfileFragment()
                binding.root.findNavController().navigate(toProfileFragment)
            }
        }
        binding.ivSetting.setOnClickListener {
            val toPengAkunFragment =
                EditProfileFragmentDirections.actionEditProfileFragmentToPengaturanAkunFragment()
            binding.root.findNavController().navigate(toPengAkunFragment)
        }
    }

//    private fun saveUrl(user: User) {
//        if (filePath != null){
//            val progressDialog = ProgressDialog(activity)
//            progressDialog.setTitle("Uploading...")
//            progressDialog.show()
//
//            val ref = storageReference.child("images_profile/" + UUID.randomUUID().toString())
//            ref.putFile(filePath)
//                .addOnSuccessListener {
//                    progressDialog.dismiss()
//                    Toast.makeText(activity,"Uploaded", Toast.LENGTH_LONG).show()
//                    ref.downloadUrl.addOnSuccessListener {
//                        saveToFirebase(it.toString(), user)
//                    }
//                }
//                .addOnFailureListener { e ->
//                    progressDialog.dismiss()
//                    Toast.makeText(activity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
//                }
//                .addOnProgressListener { taskSnapshot ->
//                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
//                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
//                }
//        }
//    }

//    private fun saveToFirebase(url: String, data: User) {
//        mDatabase.child(data.username!!).addValueEventListener(object: ValueEventListener{
//            override fun onDataChange(datasnapshot: DataSnapshot) {
//                data.url = url
//                mDatabase.child(data.username!!).setValue(data)
//                preferences.setValues("url", url)
//                preferences.setValues("nama", data.nama.toString())
//                preferences.setValues("jenis_kelamin", data.jenis_kelamin.toString())
//                preferences.setValues("umur", data.umur.toString())
//                preferences.setValues("tinggi", data.tinggi.toString())
//                preferences.setValues("berat", data.berat.toString())
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show()
//            }
//
//        })
//
//    }

//    private fun saveToFirebase(data: User) {
//
//        sUsername = preferences.getValues("username").toString()
//        mDatabase.child(sUsername).setValue(data)
//
//        preferences.setValues("nama", data.nama.toString())
//        preferences.setValues("jenis_kelamin", data.jenis_kelamin.toString())
//        preferences.setValues("umur", data.umur.toString())
//        preferences.setValues("tinggi", data.tinggi.toString())
//        preferences.setValues("berat", data.berat.toString())
//    }



    private fun edtForm() {
        sNama = binding.edtNama.text.toString()
        sJenisKelamin = binding.edtJenisKelamin.text.toString()
        sUmur = binding.edtUmur.text.toString()
        sTinggi = binding.edtTinggi.text.toString()
        sBerat = binding.edtBeratBadan.text.toString()
    }

    private fun saveUser(
        sNama: String, sJenisKelamin: String,
        sUmur: String, sTinggi: String,
        sBerat: String) {

        val user = User()
        user.nama = sNama
        user.jenis_kelamin = sJenisKelamin
        user.umur = sUmur
        user.tinggi = sTinggi
        user.berat = sBerat

        user.email = preferences.getValues("email").toString()
        user.username = preferences.getValues("username").toString()
        user.password = preferences.getValues("password").toString()
        user.url = preferences.getValues("url").toString()

        saveToFirebase(user)
    }

    private fun saveToFirebase(data: User) {
        mDatabase.child(data.username!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                mDatabase.child(data.username!!).setValue(data)

                preferences.setValues("nama", data.nama.toString())
                preferences.setValues("jenis_kelamin", data.jenis_kelamin.toString())
                preferences.setValues("umur", data.umur.toString())
                preferences.setValues("tinggi", data.tinggi.toString())
                preferences.setValues("berat", data.berat.toString())

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun killActivity() {
        activity?.finish()
    }

    private fun setUpFrom() {
        val jeniskelamin = resources.getStringArray(R.array.jenis_kelamin)
        val arrayAdapterJenisKelamin = ArrayAdapter(requireContext(), R.layout.dropdown_item, jeniskelamin)
        binding.edtJenisKelamin.setAdapter(arrayAdapterJenisKelamin)

        Glide.with(this)
            .load(preferences.getValues("url"))
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivProfile)

        binding.edtNama.setText(preferences.getValues("nama"))
        binding.edtJenisKelamin.setText(preferences.getValues("jenis_kelamin"))
        binding.edtUmur.setText(preferences.getValues("umur"))
        binding.edtTinggi.setText(preferences.getValues("tinggi"))
        binding.edtBeratBadan.setText(preferences.getValues("berat"))
    }

}