package com.example.teuniverse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MediaAdapter(private val itemList: ArrayList<MediaItem>): RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_rv_item, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.link.text = currentItem.link
        holder.date.text = currentItem.date
        holder.title.text = currentItem.title
        holder.viewCount.text = currentItem.view.toString()

        Glide.with(holder.itemView)
            .load(currentItem.thumbnail)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.thumbnail)
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
        val title: TextView = itemView.findViewById(R.id.media_title)
        val viewCount: TextView = itemView.findViewById(R.id.view_count)
        val thumbnail: ImageView = itemView.findViewById(R.id.media_img)
        val link: TextView = itemView.findViewById(R.id.video_link)
    }
}