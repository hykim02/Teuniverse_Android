package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CommunityPostAdapter(private val itemList: ArrayList<CommunityPostItem>,
                           private val navController: NavController):
    RecyclerView.Adapter<CommunityPostAdapter.CommunityPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.community_rv_item, parent, false)
        return CommunityPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityPostViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.feedId.text = currentItem.feedId.toString()
        holder.fandomName.text = currentItem.fandomName
        holder.postTerm.text = currentItem.postTerm.toString()
        holder.postSummary.text = currentItem.postSummary
        holder.heartCount.text = currentItem.heartCount.toString()
        holder.commentCount.text = currentItem.commentCount.toString()

        holder.feedId.visibility = View.GONE

        // 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(currentItem.postImg) // currentItem.img가 이미지 URL인 경우
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(holder.postImg)

        // 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(currentItem.userImg) // currentItem.img가 이미지 URL인 경우
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(holder.userImg)
}

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class CommunityPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg: ImageView = itemView.findViewById(R.id.user_profile)
        val fandomName: TextView = itemView.findViewById(R.id.fandom_name)
        val postTerm: TextView = itemView.findViewById(R.id.term)
        val postImg: ImageView = itemView.findViewById(R.id.post_img)
        val postSummary: TextView = itemView.findViewById(R.id.post_content)
        var heartCount: TextView = itemView.findViewById(R.id.heart_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)
        val feedId: TextView = itemView.findViewById(R.id.feed_id)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    // Bundle에 데이터를 담아서 NavController를 통해 전달
                    val feedId = itemList[pos].feedId
                    val bundle = Bundle().apply {
                        putString("feedId", feedId.toString())
                    }
                    navController.navigate(R.id.action_navigation_community_to_navigation_communityDetail, bundle)
                }
            }
        }
    }
}