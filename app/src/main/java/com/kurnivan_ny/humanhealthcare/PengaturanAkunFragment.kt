package com.kurnivan_ny.humanhealthcare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.google.firebase.database.*
import com.kurnivan_ny.humanhealthcare.databinding.FragmentPengaturanAkunBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.User
import com.kurnivan_ny.humanhealthcare.utils.Preferences

/**
 * A simple [Fragment] subclass.
 * Use the [PengaturanAkunFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PengaturanAkunFragment : Fragment() {

    //BINDING
    private var _binding: FragmentPengaturanAkunBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: Preferences
    lateinit var mDatabase: DatabaseReference

    private lateinit var sEmail: String
    private lateinit var sUsername: String
    private lateinit var sPasswordLama: String
    private lateinit var sPasswordBaru: String
    private lateinit var sKonfirmasiPassBaru: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPengaturanAkunBinding.inflate(inflater, container, false)
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

        setUpForm()

        itemOnClickListener()
    }

    private fun setUpForm() {
        binding.edtEmail.setText(preferences.getValues("email"))
        binding.edtUsername.setText(preferences.getValues("username"))
    }

    private fun itemOnClickListener() {
        binding.ivBackAkun.isClickable = true
        binding.ivBackAkun.setOnClickListener {
            val toEdtProfileFragment = PengaturanAkunFragmentDirections.actionPengaturanAkunFragmentToEditProfileFragment()
            binding.root.findNavController().navigate(toEdtProfileFragment)
        }

        binding.btnSimpan.setOnClickListener {

            edtForm()
            if (sEmail.equals("")){
                binding.edtEmail.error = "Silakan isi Email"
                binding.edtEmail.requestFocus()
            }
            else if (sUsername.equals("")){
                binding.edtUsername.error = "Silakan isi Username"
                binding.edtUsername.requestFocus()
            }
            else if (sPasswordLama.equals("")){
                binding.edtPasswordLama.error = "Silakan isi Password Lama"
                binding.edtPasswordLama.requestFocus()
            }
            else if (sPasswordBaru.equals("")){
                binding.edtPasswordBaru.error = "Silakan isi Password Baru"
                binding.edtPasswordBaru.requestFocus()
            }
            else if (sKonfirmasiPassBaru.equals("")){
                binding.edtKonfirmasiPass.error = "Silakan isi Konfirmasi Password Baru"
            }
            else {
                saveUser(sEmail,sUsername,sPasswordLama,sPasswordBaru,sKonfirmasiPassBaru)

                val toProfileFragment = PengaturanAkunFragmentDirections.actionPengaturanAkunFragmentToProfileFragment()
                binding.root.findNavController().navigate(toProfileFragment)
            }
        }
    }

    private fun saveUser(
        sEmail: String, sUsername: String,
        sPasswordLama: String, sPasswordBaru: String,
        sKonfirmasiPassBaru: String) {

        val user = User()

        user.username = sUsername
        user.email = sEmail
        user.password = sPasswordBaru

        if (sPasswordBaru.equals(sKonfirmasiPassBaru)){
            checkingPasswordLama(sUsername, sPasswordLama, sPasswordBaru, user)
        }
//        else {
//            Toast.makeText(activity, "Password Baru Tidak Sama Dengan Konfirmasi Password", Toast.LENGTH_LONG).show()
//        }
    }

    private fun checkingPasswordLama(sUsername: String, sPasswordLama: String,
                                     sPasswordBaru: String, data: User) {

            if (sPasswordLama.equals(preferences.getValues("password"))) {
                savetoFirebase(data)
            }
//            else {
//                Toast.makeText(activity, "Password Lama Anda Salah", Toast.LENGTH_LONG).show()
//            }
        }

    private fun savetoFirebase(data: User) {

        mDatabase.child(data.username!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                mDatabase.child(data.username!!).setValue(data)

                preferences.setValues("email", data.nama.toString())
                preferences.setValues("username", data.jenis_kelamin.toString())
                preferences.setValues("password", data.umur.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show()
            }

        })

        preferences.setValues("email", data.email.toString())
        preferences.setValues("username", data.username.toString())
        preferences.setValues("password", data.password.toString())
    }

    private fun edtForm() {
        sEmail = binding.edtEmail.text.toString()
        sUsername = binding.edtUsername.text.toString()
        sPasswordLama = binding.edtPasswordLama.text.toString()
        sPasswordBaru = binding.edtPasswordBaru.text.toString()
        sKonfirmasiPassBaru = binding.edtKonfirmasiPass.text.toString()
    }

    private fun killActivity() {
        activity?.finish()
    }
}