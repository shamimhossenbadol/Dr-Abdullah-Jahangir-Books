package com.shamimhossen.abdullah_jahangir_books.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shamimhossen.abdullah_jahangir_books.databinding.VideoItemsBinding

class VideoAdapter(
    private val activity: Activity, private val videoList: MutableList<String>
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            VideoItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.thumbnail.load("https://img.youtube.com/vi/${videoList[position]}/mqdefault.jpg")
        holder.binding.root.setOnClickListener {
            videoList[position].let { it1 -> (activity as ItemClick).onClick(it1) }
        }
    }

    class ViewHolder(val binding: VideoItemsBinding) : RecyclerView.ViewHolder(binding.root)

    internal interface ItemClick {
        fun onClick(videoId: String)
    }
}