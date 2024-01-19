package com.example.teuniverse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CommentAdapter(private val itemList: List<CommentItem>):
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_rv_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentItem = itemList[position]
        // 유저 프로필
        Glide.with(holder.itemView.context)
            .load(currentItem.userImg)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.userImg)

        holder.nickName.text = currentItem.nickName
        holder.time.text = currentItem.postTime
        holder.comment.text = currentItem.comment
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg: ImageView = itemView.findViewById(R.id.user_profile)
        val nickName: TextView = itemView.findViewById(R.id.fandom_name)
        val time: TextView = itemView.findViewById(R.id.term)
        val comment: TextView = itemView.findViewById(R.id.comment)
    }
}