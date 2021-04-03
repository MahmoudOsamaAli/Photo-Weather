package com.example.photoweather.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoweather.R
import com.example.photoweather.utils.RecyclerViewListener
import com.example.photoweather.databinding.ImageItemBinding
import java.io.File
import kotlin.collections.ArrayList


class ImagesAdapter(
    private val context: Context,
    items: ArrayList<String>?,
    private val listener: RecyclerViewListener
) :
    RecyclerView.Adapter<MyViewHolder>() {
    private var imagesPathList = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ImageItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.image_item, parent, false)
        return MyViewHolder(context, binding, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) =
        holder.bind(imagesPathList!![position])

    override fun getItemCount(): Int {
        return imagesPathList!!.size
    }

    fun updateDate(imgs: ArrayList<String>?) {
        this.imagesPathList = imgs
        notifyDataSetChanged()
    }
}

class MyViewHolder(
    private val context: Context,
    private val binding: ImageItemBinding,
    private val listener: RecyclerViewListener
) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(item: String) {
        val file = File(item)
        try {
            val uri = Uri.fromFile(file)
            Glide.with(context)
                .load(uri)
                .into(binding.img)
            binding.img.setOnClickListener {
                listener.onItemClicked(
                    adapterPosition,
                    binding.img.id
                )
            }
            binding.deleteImg.setOnClickListener {
                listener.onItemClicked(
                    adapterPosition,
                    binding.deleteImg.id
                )
            }
            binding.shareImg.setOnClickListener {
                listener.onItemClicked(
                    adapterPosition,
                    binding.shareImg.id
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            file.delete()
        }
    }

}
