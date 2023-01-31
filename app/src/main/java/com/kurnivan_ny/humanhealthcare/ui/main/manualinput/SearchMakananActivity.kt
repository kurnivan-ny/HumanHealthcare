package com.kurnivan_ny.humanhealthcare.ui.main.manualinput

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kurnivan_ny.humanhealthcare.databinding.ActivitySearchMakananBinding
import com.kurnivan_ny.humanhealthcare.ui.adapter.SearchListAdapter
import com.kurnivan_ny.humanhealthcare.data.model.manualinput.SearchModel
import com.kurnivan_ny.humanhealthcare.viewmodel.preferences.SharedPreferences

class SearchMakananActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchMakananBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var searchList: List<SearchModel> = ArrayList()
    private val searchListAdapter = SearchListAdapter(searchList)

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMakananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = SharedPreferences(this)
        db = FirebaseFirestore.getInstance()

        // Set up recycler view
        binding.rvSearchlist.hasFixedSize()
        binding.rvSearchlist.layoutManager = LinearLayoutManager(this)
        binding.rvSearchlist.adapter = searchListAdapter

        binding.svMakanan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Get Value of field
                binding.svMakanan.hint
                binding.svMakanan.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                val searchText = binding.svMakanan.text.toString()

                // Search in Firestore
                searchInFirestore(searchText.toLowerCase())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

    private fun searchInFirestore(searchText: String) {

        val username = sharedPreferences.getValuesString("username")
        val tanggal_makan = sharedPreferences.getValuesString("tanggal_makan")
        val waktu_makan = sharedPreferences.getValuesString("waktu_makan")

        db.collection("food")
            .whereArrayContains("kata_kunci", searchText)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {

                    // Get the list and set in to adapter
                    searchList = it.result!!.toObjects(SearchModel::class.java)
                    searchListAdapter.searchList = searchList
                    searchListAdapter.notifyDataSetChanged()

                    searchListAdapter.onItemClick = { food ->

                        db.collection("users").document(username!!)
                            .collection("makan").document(tanggal_makan!!)
                            .collection(waktu_makan!!).document(food.nama_makanan)
                            .get().addOnSuccessListener {
                                if (it.get("nama_makanan") == null){
                                    // To Detail Activity
                                    val intent = Intent(
                                        this@SearchMakananActivity,
                                        DetailMakananActivity::class.java
                                    )
                                    intent.putExtra("nama_makanan", food.nama_makanan)
                                    startActivity(intent)
                                }
                                else {
                                    Toast.makeText(this, "Makanan sudah diinput", Toast.LENGTH_LONG).show()
                                    binding.svMakanan.requestFocus()
                                }
                            }
                    }

                }
            }
    }
}