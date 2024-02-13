package com.example.teuniverse

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(private val itemList: ArrayList<MediaContent>): RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_rv_item, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.link.text = currentItem.url
        holder.link.visibility = GONE

        holder.date.text = currentItem.publishedAt
        holder.title.text = currentItem.title
        holder.viewCount.text = currentItem.views.toString()

        Glide.with(holder.itemView)
            .load(currentItem.thumbnailUrl)
            .into(holder.thumbnail)

        holder.thumbnail.setOnClickListener {
            // 썸네일을 클릭했을 때 실행되는 코드
            onThumbnailClick(holder.itemView, currentItem.url)
        }
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

    // 썸네일 클릭 이벤트 처리 메서드
    private fun onThumbnailClick(itemView: View, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        itemView.context.startActivity(intent)
    }
}