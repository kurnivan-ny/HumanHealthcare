package com.kurnivan_ny.humanhealthcare.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import com.kurnivan_ny.humanhealthcare.R

class LoadingDialog(val myActivity: Activity) {

    private lateinit var isDialog: AlertDialog

    fun startLoading(){

        // set View
        val inflater = myActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)

        // set Dialog
        val builder = AlertDialog.Builder(myActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()
    }

    fun isDismiss(){
        isDialog.dismiss()
    }
}