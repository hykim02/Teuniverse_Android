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

class HomeMediaAdapter(private val homeMediaList: ArrayList<HomeMediaItem>): RecyclerView.Adapter<HomeMediaAdapter.HomeMediaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_rv_media, parent, false)
        return HomeMediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeMediaViewHolder, position: Int) {
        val currentItem = homeMediaList[position]

        holder.url.text = currentItem.url
        holder.url.visibility = GONE

        Glide.with(holder.itemView)
            .load(currentItem.thumbnailUrl)
            .into(holder.thumbnail)

        holder.thumbnail.setOnClickListener {
            // 썸네일을 클릭했을 때 실행되는 코드
            onThumbnailClick(holder.itemView, currentItem.url)
        }
    }

    override fun getItemCount(): Int {
        return homeMediaList.count()
    }

    inner class HomeMediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val url: TextView = itemView.findViewById(R.id.media_url)
        val thumbnail: ImageView = itemView.findViewById(R.id.media_img)
    }

    // 썸네일 클릭 이벤트 처리 메서드
    private fun onThumbnailClick(itemView: View, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        itemView.context.startActivity(intent)
    }
}