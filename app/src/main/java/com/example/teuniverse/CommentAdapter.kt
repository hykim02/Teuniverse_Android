package com.example.teuniverse

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CommentAdapter(private val itemList: List<CommentItem>,
                     private val lifecycleOwner: LifecycleOwner
):
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
        holder.commentId.visibility = View.GONE

        // 게시물 삭제 및 수정
        holder.optionBtn.setOnClickListener { view ->
            showPopupMenu(view, currentItem)
        }
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg: ImageView = itemView.findViewById(R.id.user_profile)
        val nickName: TextView = itemView.findViewById(R.id.fandom_name)
        val time: TextView = itemView.findViewById(R.id.term)
        val comment: TextView = itemView.findViewById(R.id.comment)
        val commentId: TextView = itemView.findViewById(R.id.commentId)
        val optionBtn: ImageButton = itemView.findViewById(R.id.btn_option)
    }

    // 댓글 삭제 api
    private suspend fun deleteCommentApi(commentId: Int, view: View) {
        Log.d("createCommentApi 함수", "호출 성공")
        val accessToken = getAccessToken(view)
        try {
            if (accessToken != null) {
                val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                    DeleteCommentInstance.deleteCommentService().deleteComment(commentId, accessToken)
                }
                if (response.isSuccessful) {
                    val theComment: SignUpResponse? = response.body()
                    if (theComment != null) {
                        Log.d("deleteCommentApi 함수 response", "${theComment.statusCode} ${theComment.message}")
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("deleteCommentApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Api 함수 Error", errorMessage)
    }

    // 댓글 삭제 및 수정 옵션
    private fun showPopupMenu(view: View, item: CommentItem) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.option_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    // 삭제 버튼 클릭 시 처리
                    Log.d("삭제할 댓글id",item.commendId.toString())
                    lifecycleOwner.lifecycleScope.launch {
                        deleteCommentApi(item.commendId, view)
                    }
                    Toast.makeText(view.context,"댓글이 삭제되었습니다.",Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.edit -> {
                    // 수정 버튼 클릭 시 처리
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
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