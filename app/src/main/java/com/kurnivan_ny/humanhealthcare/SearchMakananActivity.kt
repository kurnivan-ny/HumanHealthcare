package com.kurnivan_ny.humanhealthcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kurnivan_ny.humanhealthcare.databinding.ActivitySearchMakananBinding
import com.kurnivan_ny.humanhealthcare.utils.SearchListAdapter
import com.kurnivan_ny.humanhealthcare.utils.SearchModel

class SearchMakananActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySearchMakananBinding

    private var searchList: List<SearchModel> = ArrayList()
    private val searchListAdapter = SearchListAdapter(searchList)

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMakananBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                val searchText = binding.svMakanan.text.toString()

                // Search in Firestore
                searchInFirestore(searchText.toLowerCase())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }
    private fun searchInFirestore(searchText: String) {
        db.collection("food")
            .whereArrayContains("kata_kunci", searchText)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    // Get the list and set in to adapter
                    searchList = it.result!!.toObjects(SearchModel::class.java)
                    searchListAdapter.searchList = searchList
                    searchListAdapter.notifyDataSetChanged()

                    // To Detail Activity
                    searchListAdapter.onItemClick = {
                        val intent =
                            Intent(this@SearchMakananActivity, DetailMakananActivity::class.java)
                        intent.putExtra("nama_makanan", it.nama_makanan.toString())
                        startActivity(intent)
                    }
                }
            }
    }
}