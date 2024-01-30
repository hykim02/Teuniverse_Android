package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CommunityPostAdapter(private val itemList: ArrayList<CommunityPostItem>,
                           private val navController: NavController,
                           private val lifecycleOwner: LifecycleOwner):
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

        // 게시물 삭제 및 수정
        holder.optionBtn.setOnClickListener { view ->
            showPopupMenu(view, currentItem)
        }
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
        val optionBtn: ImageButton = itemView.findViewById(R.id.btn_option)

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

    // 피드 삭제 및 수정 옵션
    private fun showPopupMenu(view: View, item: CommunityPostItem) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.option_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    // 삭제 버튼 클릭 시 처리
                    Log.d("삭제할 피드id",item.feedId.toString())
                    lifecycleOwner.lifecycleScope.launch {
                        deleteFeedApi(item.feedId.toString(), view)
                    }
                    true
                }
                R.id.edit -> {
                    // 수정 버튼 클릭 시 처리
                    editEvent(item, view)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun editEvent(item: CommunityPostItem, view: View) {
        val intent = Intent(view.context, CommunityEditActivity::class.java)
        val bundle = Bundle().apply {
            putInt("feedId", item.feedId)
            putString("postImg", item.postImg.toString())
            putString("postSummary", item.postSummary.toString())
        }
        intent.putExtras(bundle)
        view.context.startActivity(intent)
    }

    private suspend fun deleteFeedApi(feedId: String, view: View) {
        Log.d("deleteFeedsApi 함수", "호출 성공")
        val accessToken = getAccessToken(view)
        try {
            if (accessToken != null) {
                val response: Response<SignUpResponse> = withContext(
                    Dispatchers.IO) {
                    DeleteFeedInstance.deleteFeedService().deleteFeed(feedId, accessToken)
                }
                if (response.isSuccessful) {
                    val theDeleteFeed: SignUpResponse? = response.body()
                    if (theDeleteFeed != null) {
                        Log.d("deleteFeedApi 함수 response", "${theDeleteFeed.statusCode} ${theDeleteFeed.message}")
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("deleteFeedApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleError(errorMessage: String) {
        Log.d("deleteFeedApi 함수 Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(view: View): String? {
        MainActivity.ServiceAccessTokenDB.init(view.context)
        val serviceTokenDB = MainActivity.ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }
}