package com.kurnivan_ny.humanhealthcare.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kurnivan_ny.humanhealthcare.databinding.ItemSlideBinding
import com.kurnivan_ny.humanhealthcare.data.model.onboarding.ImageData

class ImageSliderAdapter(private val items: List<ImageData>): RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(itemView: ItemSlideBinding): RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun bind(data: ImageData){
            with(binding){
                Glide.with(itemView)
                    .load(data.imageList)
                    .into(ivSlider)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(ItemSlideBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int{
        return items.size
    }
}