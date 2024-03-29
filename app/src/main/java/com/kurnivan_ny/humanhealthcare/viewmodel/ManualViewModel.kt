package com.kurnivan_ny.humanhealthcare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kurnivan_ny.humanhealthcare.data.model.manualinput.ListManualModel


class ManualViewModel: ViewModel() {
    val newmanual = MutableLiveData<ArrayList<ListManualModel>>(arrayListOf())
}