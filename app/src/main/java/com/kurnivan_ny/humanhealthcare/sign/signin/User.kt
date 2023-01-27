package com.kurnivan_ny.humanhealthcare.sign.signin

class User {
    var email:String?= null
    var username:String?= null
    var password:String?= null

    var nama:String?= null
    var url:String?= null

    var jenis_kelamin:String?= null
    var umur:Int?= null
    var tinggi:Int?= null
    var berat:Int?= null
    var totalenergikal:Float?= null
}

class Konsumsi {
    var tanggal_makan:String? = null

    var total_konsumsi_kabohidrat:Float?= null
    var total_konsumsi_protein:Float?= null
    var total_konsumsi_lemak:Float?= null

    var status_konsumsi_karbohidrat:String? = null
    var status_konsumsi_protein:String? = null
    var status_konsumsi_lemak:String? = null
}

class Makan {
    var waktu_makan:String? = null
}
