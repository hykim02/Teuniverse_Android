package com.example.teuniverse

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CommunityPostAdapter(private val itemList: ArrayList<CommunityPostItem>):
    RecyclerView.Adapter<CommunityPostAdapter.CommunityPostViewHolder>() {

//    private var mrecyclerview : RecyclerView? = null
//
//    override fun onAttachedToRecyclerView(recyclerview: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerview)
//        mrecyclerview = recyclerview
//    }
//
//    override fun onDetachedFromRecyclerView(recyclerview: RecyclerView) {
//        super.onDetachedFromRecyclerView(recyclerview)
//        mrecyclerview = null// to avoid memory leak
//    }
    //커스텀 리스너
    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int) // 추상 메소드
    }
    //객체 저장 변수
    private lateinit var mOnItemClickListener: OnItemClickListener
    //객체 전달 메서드
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.community_rv_item, parent, false)
        return CommunityPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityPostViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.fandomName.text = currentItem.fandomName
        holder.postTerm.text = currentItem.postTerm.toString()
        holder.postSummary.text = currentItem.postSummary
        holder.heartCount.text = currentItem.heartCount.toString()
        holder.commentCount.text = currentItem.commentCount.toString()

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

//        holder.itemView.setOnClickListener {
//            val action = CommunityFragmentDirections.actionNavigationCommunityToCommunityDetailFragment()
//            val navController = Navigation.findNavController(mrecyclerview!!)
//            navController.navigate(action)
//        }

}

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class CommunityPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg: ImageView = itemView.findViewById(R.id.user_profile)
        val fandomName: TextView = itemView.findViewById(R.id.fandom_name)
        val postTerm: TextView = itemView.findViewById(R.id.term)
        val postImg: ImageView = itemView.findViewById(R.id.post_img)
        val postSummary: TextView = itemView.findViewById(R.id.post_summary)
        var heartCount: TextView = itemView.findViewById(R.id.heart_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    mOnItemClickListener.onItemClick(itemView, pos)
                }
            }
        }
    }
}