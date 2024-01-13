package com.example.teuniverse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class CommunityPostAdapter(private val itemList: ArrayList<CommunityPostItem>) :
    RecyclerView.Adapter<CommunityPostAdapter.CommunityPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.community_rv_item, parent, false)
        return CommunityPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityPostViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.fandom_name.text = currentItem.fandomName
        holder.post_term.text = currentItem.postTerm.toString()
        holder.post_summary.text = currentItem.postSummary
//        holder.heart_count.text = currentItem.heartCount
//        holder.comment_count.text = currentItem.commentCount
        holder.post_img.setImageResource(currentItem.postImg)

        // holder.user_img.setImageResource(R.drawable.user_placeholder)
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }


    inner class CommunityPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user_img = itemView.findViewById<ImageView>(R.id.user_profile)
        val fandom_name = itemView.findViewById<TextView>(R.id.fandom_name)
        val post_term = itemView.findViewById<TextView>(R.id.term)
        val post_img = itemView.findViewById<ImageView>(R.id.post_img)
        val post_summary = itemView.findViewById<TextView>(R.id.post_summary)
        val heart_count = itemView.findViewById<ImageView>(R.id.heart_count)
        val comment_count = itemView.findViewById<ImageView>(R.id.comment_count)
    }
}