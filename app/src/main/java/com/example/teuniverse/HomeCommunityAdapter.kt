package com.example.teuniverse

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HomeCommunityAdapter(private val homeCommunityList: ArrayList<HomeCommunityItem>): RecyclerView.Adapter<HomeCommunityAdapter.HomeCommunityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCommunityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_rv_community, parent, false)
        return HomeCommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeCommunityViewHolder, position: Int) {
        val currentItem = homeCommunityList[position]

        holder.id.text = currentItem.id.toString()
        holder.title.text = currentItem.title

        Glide.with(holder.itemView)
            .load(currentItem.postImg)
            .into(holder.postImg)
    }

    override fun getItemCount(): Int {
        return homeCommunityList.count()
    }

    inner class HomeCommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.post_id)
        val title: TextView = itemView.findViewById(R.id.post_title)
        val postImg: ImageView = itemView.findViewById(R.id.post_img)
    }
}