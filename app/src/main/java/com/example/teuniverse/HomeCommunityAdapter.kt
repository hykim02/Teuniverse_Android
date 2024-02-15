package com.example.teuniverse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HomeCommunityAdapter(private val homeCommunityList: ArrayList<HomeCommunityItem>,
                           private val navController: NavController
): RecyclerView.Adapter<HomeCommunityAdapter.HomeCommunityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCommunityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_rv_community, parent, false)
        return HomeCommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeCommunityViewHolder, position: Int) {
        val currentItem = homeCommunityList[position]

        holder.id.text = currentItem.id.toString()
        holder.id.visibility = GONE
        holder.title.text = currentItem.content

        Glide.with(holder.itemView)
            .load(currentItem.thumbnailUrl)
            .into(holder.postImg)
    }

    override fun getItemCount(): Int {
        return homeCommunityList.count()
    }

    inner class HomeCommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.post_id)
        val title: TextView = itemView.findViewById(R.id.post_title)
        val postImg: ImageView = itemView.findViewById(R.id.post_img)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    // Bundle에 데이터를 담아서 NavController를 통해 전달
                    val feedId = homeCommunityList[pos].id
                    val bundle = Bundle().apply {
                        putString("feedId", feedId.toString())
                    }
                    navController.navigate(R.id.action_navigation_home_to_navigation_communityDetail, bundle)
                }
            }
        }
    }
}