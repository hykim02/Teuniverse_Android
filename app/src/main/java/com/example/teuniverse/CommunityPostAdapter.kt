package com.example.teuniverse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
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
import java.io.File

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
        HeartStateDB.init(holder.itemView.context)

        initHeartState(holder, currentItem) // 하트 상태 초기화

        holder.feedId.text = currentItem.feedId.toString()
        holder.fandomName.text = currentItem.fandomName
        holder.postTerm.text = currentItem.postTerm.toString()
        holder.postSummary.text = currentItem.postSummary
        holder.heartCount.text = currentItem.heartCount.toString()
        holder.commentCount.text = currentItem.commentCount.toString()

        holder.feedId.visibility = GONE

        if (currentItem.postImg == null) {
            holder.postImg.visibility = GONE
        }

        // 본문 내용 글자 수가 85 이하라면
        if (currentItem.postSummary != null && currentItem.postSummary.length <= 85) {
            holder.postSummary.text = currentItem.postSummary
            holder.moreBtn.visibility = GONE
        } else if (currentItem.postSummary != null) {
            val truncatedPostSummary = currentItem.postSummary.substring(0, 85) + "..."
            holder.postSummary.text = truncatedPostSummary
            holder.moreBtn.visibility = View.VISIBLE

            // 더보기 클릭 시 모든 내용 확인 가능하도록
            holder.moreBtn.setOnClickListener {
                holder.postSummary.text = currentItem.postSummary
                holder.moreBtn.visibility = GONE
            }
        }

        // 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(currentItem.postImg) // currentItem.img가 이미지 URL인 경우
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

        holder.likeBtn.setOnClickListener { view ->
            setHeartState(holder, currentItem, view)
        }

        holder.moreBtn.setOnClickListener {
            holder.postSummary.text = currentItem.postSummary
        }
    }

    // 하트 상태 설정
    private fun setHeartState(holder: CommunityPostViewHolder, currentItem: CommunityPostItem, view: View) {
        HeartStateDB.init(holder.itemView.context)
        val editor = HeartStateDB.getInstance().edit()
        val getData = HeartStateDB.getInstance().all
        val sharedPrefsFile = File("${holder.itemView.context.filesDir.parent}/shared_prefs/HeartState.xml")
        val isExist = sharedPrefsFile.exists()

        // heart 파일이 존재한다면
        if (isExist) {
            if (getData.containsKey("${currentItem.feedId}")) { // 해당 키가 존재한다면
                val heartState = getData.getValue("${currentItem.feedId}") // feedID에 해당하는 하트 상태 값

                if (heartState == true) { // 하트가 이미 클릭된 상태
                    holder.likeBtn.setImageResource(R.drawable.icon_heart_off)
                    lifecycleOwner.lifecycleScope.launch {
                        cancelClickLikeApi(currentItem.feedId, view, holder)
                    }
                    editor.putBoolean("${currentItem.feedId}", false)
                    editor.apply()
                } else {
                    holder.likeBtn.setImageResource(R.drawable.icon_heart_on)
                    lifecycleOwner.lifecycleScope.launch {
                        clickLikeApi(currentItem.feedId, view, holder)
                    }
                    editor.putBoolean("${currentItem.feedId}", true)
                    editor.apply()
                }
            } else { // 파일은 존재하지만 해당 키가 존재하지 않는다면
                holder.likeBtn.setImageResource(R.drawable.icon_heart_on)
                lifecycleOwner.lifecycleScope.launch {
                    clickLikeApi(currentItem.feedId, view, holder)
                }
                editor.putBoolean("${currentItem.feedId}", true)
                editor.apply()
            }
        } else {  // 존재하지 않는다면 (최초 클릭)
            holder.likeBtn.setImageResource(R.drawable.icon_heart_on)
            lifecycleOwner.lifecycleScope.launch {
                clickLikeApi(currentItem.feedId, view, holder)
            }
            editor.putBoolean("${currentItem.feedId}", true)
            editor.apply()
        }
    }

    // 하트 상태 초기화
    private fun initHeartState(holder: CommunityPostViewHolder,  currentItem: CommunityPostItem) {
        HeartStateDB.init(holder.itemView.context)
        val getData = HeartStateDB.getInstance().all
        val sharedPrefsFile = File("${holder.itemView.context.filesDir.parent}/shared_prefs/HeartState.xml")
        val isExist = sharedPrefsFile.exists()

        if (isExist) {
            for ((key, value) in getData.entries) {
                if (key == currentItem.feedId.toString()) {
                    val isLiked = value as? Boolean ?: false

                    if (isLiked) {
                        holder.likeBtn.setImageResource(R.drawable.icon_heart_on)
                    } else {
                        holder.likeBtn.setImageResource(R.drawable.icon_heart_off)
                    }
                }
            }
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
        val likeBtn: ImageButton = itemView.findViewById(R.id.like)
        val moreBtn: TextView = itemView.findViewById(R.id.more)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    // Bundle에 데이터를 담아서 NavController를 통해 전달
                    val feedId = itemList[pos].feedId
                    val bundle = Bundle().apply {
                        putString("feedId", feedId.toString())
                        putString("postImg", itemList[pos].postImg)
                    }
                    // 현재 프래그먼트에 따라서 다른 목적지로 이동하도록 처리
                    when (navController.currentDestination?.id) {
                        R.id.navigation_community -> {
                            navController.navigate(R.id.action_navigation_community_to_navigation_communityDetail, bundle)
                        }
                        R.id.navigation_profile -> {
                            navController.navigate(R.id.action_navigation_profile_to_navigation_communityDetail, bundle)
                        }
                    }
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
                    showPopupDeleteDialog(view, item.feedId.toString())
                    true
                }
                R.id.edit -> {
                    // 수정 버튼 클릭 시 처리
                    editEvent(item, view)
                    Log.d("수정할 피드id", item.feedId.toString())
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
            putString("postImg", item.postImg)
            putString("postSummary", item.postSummary)
        }
        intent.putExtras(bundle)
        view.context.startActivity(intent)
    }

    // 피드 삭제 api
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
                        Toast.makeText(view.context, "피드 삭제 성공", Toast.LENGTH_SHORT).show()
                        Log.d("deleteFeedApi 함수 response", "${theDeleteFeed.statusCode} ${theDeleteFeed.message}")
                    } else {
                        Toast.makeText(view.context, "피드 삭제 실패", Toast.LENGTH_SHORT).show()
                        handleError("Response body is null.")
                    }
                } else {
                    Toast.makeText(view.context, "피드 삭제 실패", Toast.LENGTH_SHORT).show()
                    handleError("deleteFeedApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 좋아요 생성
    private suspend fun clickLikeApi(feedId: Int, view: View, holder: CommunityPostViewHolder) {
        Log.d("clickLikeApi 함수", "호출 성공")
        val accessToken = getAccessToken(view)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CreateHeart>> = withContext(
                    Dispatchers.IO) {
                    ClickLikeInstance.clickLikeService().clickLike(feedId, accessToken)
                }
                if (response.isSuccessful) {
                    val theLike: ServerResponse<CreateHeart>? = response.body()
                    if (theLike != null) {
                        Log.d("clickLikeApi 함수 response", "${theLike.statusCode} ${theLike.message}")
                        likeHandleResponse(theLike, holder)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("clickLikeApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 좋아요 취소
    private suspend fun cancelClickLikeApi(feedId: Int, view: View, holder: CommunityPostViewHolder) {
        Log.d("cancelClickLikeApi 함수", "호출 성공")
        val accessToken = getAccessToken(view)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CreateHeart>> = withContext(
                    Dispatchers.IO) {
                    CancelClickLikeInstance.cancelClickLikeService().cancelClickLike(feedId, accessToken)
                }
                if (response.isSuccessful) {
                    val theLike: ServerResponse<CreateHeart>? = response.body()
                    if (theLike != null) {
                        Log.d("cancelClickLikeApi 함수 response", "${theLike.statusCode} ${theLike.message}")
                        likeHandleResponse(theLike, holder)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("cancelClickLikeApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun likeHandleResponse(theLike: ServerResponse<CreateHeart>, holder: CommunityPostViewHolder) {
        holder.heartCount.text = theLike.data.likeCount.toString()
    }

    private fun handleError(errorMessage: String) {
        Log.d("Api 함수 Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(view: View): String? {
        ServiceAccessTokenDB.init(view.context)
        val serviceTokenDB = ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }

    private fun showPopupDeleteDialog(view: View, feedId: String) {
        val popupDelete = PopupDelete(view.context, feedId)
        popupDelete.show()

        popupDelete.setOnDismissListener {
            Log.d("popupDelete", "PopupVote dialog dismissed.")
        }
    }
}