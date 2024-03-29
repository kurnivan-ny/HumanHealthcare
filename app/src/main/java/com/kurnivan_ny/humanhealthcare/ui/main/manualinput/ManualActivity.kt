package com.kurnivan_ny.humanhealthcare.ui.main.manualinput

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.kurnivan_ny.humanhealthcare.data.model.manualinput.ListManualModel
import com.kurnivan_ny.humanhealthcare.databinding.ActivityManualBinding
import com.kurnivan_ny.humanhealthcare.ui.adapter.ListManualAdapter
import com.kurnivan_ny.humanhealthcare.ui.adapter.OnItemClickListener
import com.kurnivan_ny.humanhealthcare.ui.dialog.LoadingDialog
import com.kurnivan_ny.humanhealthcare.ui.main.HomeActivity
import com.kurnivan_ny.humanhealthcare.viewmodel.ManualViewModel
import com.kurnivan_ny.humanhealthcare.viewmodel.preferences.SharedPreferences
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ManualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: FirebaseFirestore

    private var manualList: ArrayList<ListManualModel> = arrayListOf()
    private var manualListAdapter: ListManualAdapter = ListManualAdapter(manualList)

    private lateinit var viewModel: ManualViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = SharedPreferences(this)
        db = FirebaseFirestore.getInstance()

        viewModel = ViewModelProvider(this).get(ManualViewModel::class.java)

        binding.tvWaktuMakan.text = capitalize(sharedPreferences.getValuesString("waktu_makan"))

        binding.rvHasil.setHasFixedSize(true)
        binding.rvHasil.layoutManager = LinearLayoutManager(this)

        getDataFirestore()
        viewModel.newmanual.observe(this, Observer {
//            manualListAdapter.updateData(it)
            manualListAdapter.manualList = it
            manualListAdapter.notifyDataSetChanged()
        })

        binding.rvHasil.adapter = manualListAdapter

        loadingDialog()

        binding.rvHasil.itemAnimator = SlideInUpAnimator()

        binding.svMakanan.isFocusable = false

        binding.svMakanan.setOnClickListener {
            val intent = Intent(this, SearchMakananActivity::class.java)
            startActivity(intent)
        }

        binding.btnKirim.setOnClickListener {
            val username = sharedPreferences.getValuesString("username")
            val tanggal_makan = sharedPreferences.getValuesString("tanggal_makan")
            val bulan_makan = sharedPreferences.getValuesString("bulan_makan")

            db.collection("users").document(username!!)
                .collection(bulan_makan!!).document(tanggal_makan!!)
                .get().addOnSuccessListener {
                    var total_karbohidrat: Float =
                        (it.get("total_konsumsi_karbohidrat").toString() + "F").toFloat()
                    var total_protein: Float =
                        (it.get("total_konsumsi_protein").toString() + "F").toFloat()
                    var total_lemak: Float =
                        (it.get("total_konsumsi_lemak").toString() + "F").toFloat()

                    sharedPreferences.setValuesFloat(
                        "total_konsumsi_karbohidrat",
                        total_karbohidrat
                    )
                    sharedPreferences.setValuesFloat("total_konsumsi_protein", total_protein)
                    sharedPreferences.setValuesFloat("total_konsumsi_lemak", total_lemak)
                }

            val intent = Intent(this@ManualActivity, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        manualListAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int, nama_makanan: String) {
                val intent = Intent(this@ManualActivity, EditDetailMakananActivity::class.java)
                intent.putExtra("nama_makanan", nama_makanan)
                startActivity(intent)
                manualListAdapter.notifyDataSetChanged()
                finishAffinity()
            }
        })
    }

    private fun loadingDialog() {
        val loading = LoadingDialog(this)
        loading.startLoading()
        val handler = Handler()
        handler.postDelayed(object: Runnable{
            override fun run() {
                loading.isDismiss()
            }
        }, 2000)
    }

    private fun getDataFirestore() {
        val username = sharedPreferences.getValuesString("username")
        val tanggal_makan = sharedPreferences.getValuesString("tanggal_makan")
        val waktu_makan = sharedPreferences.getValuesString("waktu_makan")
        val bulan_makan = sharedPreferences.getValuesString("bulan_makan")

        db.collection("users").document(username!!)
            .collection(bulan_makan!!).document(tanggal_makan!!)
            .collection(waktu_makan!!).orderBy("nama_makanan", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot>{
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?) {
                    if (error != null){
                        Log.e("Firestore Error", error.message.toString())
                    }

                    val doc = arrayListOf<ListManualModel>()
//                    for (dc in value?.documentChanges!!){
//                        if (dc.type == DocumentChange.Type.ADDED){
////                            manualList.add(dc.document.toObject(ListManualModel::class.java))
//                            val data = dc.document.toObject(ListManualModel::class.java)
////                            manualList.add(data)
//                            doc.add(data)
////                            viewModel.addDocument(data)
////                            viewModel.newmanual.value!!.add(dc.document.toObject(ListManualModel::class.java))
//                        }
//                        else{
//                        val data = dc.document.toObject(ListManualModel::class.java)
//                        doc.add(data)}
//                    }
                    if (value != null){
                        for(dc in value.documents){
                            val data = dc.toObject(ListManualModel::class.java)
                            if (data != null) {
                                doc.add(data)
                            }
                        }
                    }
                    viewModel.newmanual.value = doc
                    manualListAdapter.notifyDataSetChanged()
                }
            })

        manualListAdapter.username = username
        manualListAdapter.tanggal_makan = tanggal_makan
        manualListAdapter.bulan_makan = bulan_makan

    }

    private fun capitalize(str: String?): CharSequence? {
        return str?.trim()?.split("\\s+".toRegex())
            ?.map { it.capitalize() }?.joinToString(" ")
    }
}