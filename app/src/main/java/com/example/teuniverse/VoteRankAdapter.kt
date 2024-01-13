package com.example.teuniverse

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VoteRankAdapter(private val itemList: ArrayList<VoteRankingItem>):
RecyclerView.Adapter<VoteRankAdapter.VoteRankViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vote_rv_item, parent, false)
        return VoteRankViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoteRankViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.rank.text = currentItem.rank
        holder.name.text = currentItem.name
        holder.count.text = currentItem.count

        // 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(currentItem.img) // currentItem.img가 이미지 URL인 경우
            .into(holder.img)
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }


    inner class VoteRankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rank: TextView = itemView.findViewById(R.id.ranking)
        val img: ImageView = itemView.findViewById(R.id.ranking_img)
        val name: TextView = itemView.findViewById(R.id.ranking_name)
        val count: TextView = itemView.findViewById(R.id.ranking_count)
    }
}