package com.kurnivan_ny.humanhealthcare.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kurnivan_ny.humanhealthcare.data.model.history.ListHistoryModel

class HistoryViewModel: ViewModel() {
    val newhistory = MutableLiveData<ArrayList<ListHistoryModel>>(arrayListOf())
}