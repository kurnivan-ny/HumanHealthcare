package com.kurnivan_ny.humanhealthcare.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.data.model.history.ListHistoryModel
import com.kurnivan_ny.humanhealthcare.databinding.ListHistoryBinding


class ListHistoryAdapter(var historyList: ArrayList<ListHistoryModel>):
    RecyclerView.Adapter<ListHistoryAdapter.ListHistoryViewModel>() {


    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    inner class ListHistoryViewModel(private val itemBinding: ListHistoryBinding):
        RecyclerView.ViewHolder(itemBinding.root){
        fun bind(listHistoryModel: ListHistoryModel){

            itemBinding.tvTitle.text = listHistoryModel.tanggal_makan

            if (listHistoryModel.status_konsumsi_karbohidrat.equals("Kurang") or
                    listHistoryModel.status_konsumsi_protein.equals("Kurang") or
                    listHistoryModel.status_konsumsi_lemak.equals("Kurang")){
                itemBinding.tvDescription.text = "Kurang"
                itemBinding.ivBatas.setImageResource(R.drawable.ic_kurang_batas)
            } else if (listHistoryModel.status_konsumsi_karbohidrat.equals("Normal") and
                listHistoryModel.status_konsumsi_protein.equals("Normal") and
                listHistoryModel.status_konsumsi_lemak.equals("Normal")){
                itemBinding.tvDescription.text = "Normal"
                itemBinding.ivBatas.setImageResource(R.drawable.ic_normal)
            } else if (listHistoryModel.status_konsumsi_karbohidrat.equals("Lebih") or
                listHistoryModel.status_konsumsi_protein.equals("Lebih") or
                listHistoryModel.status_konsumsi_lemak.equals("Lebih")){
                itemBinding.tvDescription.text = "Lebih"
                itemBinding.ivBatas.setImageResource(R.drawable.ic_lebih_batas)
            }
        }
        init {
            itemBinding.btnNext.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onItemClick(position, historyList[position].tanggal_makan)
                    notifyItemChanged(position)
                    notifyDataSetChanged()
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: ListHistoryViewModel, position: Int) {
        val history: ListHistoryModel = historyList[position]
        holder.bind(history)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ListHistoryViewModel {
        val view = ListHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListHistoryViewModel(view)
    }
}