package com.kurnivan_ny.humanhealthcare.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel: ViewModel(){
    val total_karbohidrat_konsumsi: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }

    val total_protein_konsumsi: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }

    val total_lemak_konsumsi: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }

    val tanggal_makan: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}