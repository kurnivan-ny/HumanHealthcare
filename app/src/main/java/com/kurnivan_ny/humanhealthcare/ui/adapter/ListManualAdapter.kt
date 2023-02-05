package com.kurnivan_ny.humanhealthcare.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.kurnivan_ny.humanhealthcare.data.model.manualinput.ListManualModel
import com.kurnivan_ny.humanhealthcare.databinding.ListItemManualBinding

class ListManualAdapter(var manualList: ArrayList<ListManualModel>): RecyclerView.Adapter<ListManualAdapter.ListManualViewHolder>() {

    var onItemClick: ((ListManualModel) -> Unit)? = null

    var username: String = ""
    var tanggal_makan: String = ""

    inner class ListManualViewHolder(private val itemBinding: ListItemManualBinding): RecyclerView.ViewHolder(itemBinding.root){
        fun bind(listManualModel: ListManualModel, position: Int){

            itemBinding.tvTitle.text = listManualModel.nama_makanan
            itemBinding.tvDescription.text = listManualModel.berat_makanan.toString()+ " " + listManualModel.satuan_makanan

            itemBinding.btnCancel.setOnClickListener {
                deleteItem(position)
                deleteFirestore(listManualModel)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListManualViewHolder {
        val view = ListItemManualBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListManualViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListManualViewHolder, position: Int) {
        val makanan:ListManualModel = manualList[position]
        holder.bind(makanan, position)

        holder.itemView.setOnClickListener {
            notifyItemChanged(position)
            onItemClick?.invoke(makanan)
            notifyItemChanged(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return manualList.size
    }

    fun deleteItem(position: Int){
        manualList.removeAt(position)
        notifyItemChanged(position)
        notifyItemRangeChanged(position,manualList.size)
        notifyDataSetChanged()
    }

    fun deleteFirestore(listManualModel: ListManualModel) {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        db.collection("users").document(username)
            .collection("makan").document(tanggal_makan)
            .collection(listManualModel.waktu_makan).document(listManualModel.nama_makanan)
            .delete().addOnSuccessListener {

                db.collection("users").document(username)
                    .collection("makan").document(tanggal_makan)
                    .get().addOnSuccessListener {
                        val total_konsumsi_karbohidrat:Float = (it.get("total_konsumsi_karbohidrat").toString()+"F").toFloat()
                        var total_karbohidrat = total_konsumsi_karbohidrat - listManualModel.karbohidrat

                        val total_konsumsi_protein:Float = (it.get("total_konsumsi_protein").toString()+"F").toFloat()
                        val total_protein = total_konsumsi_protein - listManualModel.protein

                        val total_konsumsi_lemak:Float = (it.get("total_konsumsi_lemak").toString()+"F").toFloat()
                        val total_lemak = total_konsumsi_lemak - listManualModel.lemak

                        db.collection("users").document(username)
                            .collection("makan").document(tanggal_makan)
                            .update("total_konsumsi_karbohidrat",total_karbohidrat,
                                "total_konsumsi_protein", total_protein,
                                "total_konsumsi_lemak", total_lemak)
                    }

            }
    }

}