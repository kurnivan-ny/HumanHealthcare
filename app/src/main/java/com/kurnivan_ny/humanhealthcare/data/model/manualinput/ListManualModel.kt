package com.kurnivan_ny.humanhealthcare.data.model.manualinput

data class ListManualModel (
    var nama_makanan: String = "",
    var berat_makanan: Int = 0,
    var satuan_makanan: String="",
    var karbohidrat: Float = 0.0F,
    var protein: Float = 0.0F,
    var lemak: Float = 0.0F,
    var waktu_makan: String = ""
)