package com.kurnivan_ny.humanhealthcare.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kurnivan_ny.humanhealthcare.databinding.SearchItemBinding

class SearchListAdapter(var searchList: List<SearchModel>): RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>() {

    var onItemClick: ((SearchModel) -> Unit)? = null

    class SearchListViewHolder(private val itemBinding: SearchItemBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(searchModel: SearchModel){
            itemBinding.singleItemFood.text = searchModel.nama_makanan
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchListViewHolder {
///        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_single_item, parent, false)
        val view = SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchListViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        val food = searchList[position]
        holder.bind(food)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(food)
        }
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}