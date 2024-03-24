import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.teuniverse.GiveVoteInstance
import com.example.teuniverse.NumberOfVote
import com.example.teuniverse.PopupVoteData
import com.example.teuniverse.PopupVoteInstance
import com.example.teuniverse.ServerResponse
import com.example.teuniverse.ServiceAccessTokenDB
import com.example.teuniverse.VoteMission
import com.example.teuniverse.VoteMissionDB
import com.example.teuniverse.databinding.PopupVoteCheckBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class PopupVoteCheck(
    context: Context,
    private val voteCount: Int?,
    private val voteMissionListener: VoteMissionListener
) : Dialog(context) {
    private lateinit var binding: PopupVoteCheckBinding

    interface VoteMissionListener {
        fun giveVote(voteCount: Int)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 팝업창 띄움
        binding = PopupVoteCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalScope.launch {
            getPopupVoteApi(voteCount)
        }

        VoteMissionDB.init(context)
        val db = VoteMissionDB.getInstance()

        binding.okBtn.setOnClickListener {
            val count = db.getInt("vote", 0)
            Log.d("vote", count.toString())
            if(count == 0) {
                GlobalScope.launch {
                    voteMissionApi(2, 0, count) // 투표하기 미션 2표(1회)
                }
            }
            dismiss()
        }
        initViews()
    }

    private fun initViews() = with(binding) {
        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지지 않도록
        setCancelable(false)

        // background를 투명하게 만듦
        // (중요) Dialog는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius의 적용이 보이지 않는다.
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    // 투표권 지급 미션 api
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun voteMissionApi(voteCount: Int, type: Int, count: Int) {
        Log.d("voteMissionApi", "호출 성공")
        val accessToken = getAccessToken()
        val params = VoteMission(voteCount = voteCount, type = type)
        val handler = Handler(Looper.getMainLooper())
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(
                    Dispatchers.IO) {
                    GiveVoteInstance.giveVoteService().giveVote(accessToken, params)
                }
                if (response.isSuccessful) {
                    val theVotes: ServerResponse<NumberOfVote>? = response.body()
                    if (theVotes != null) {
                        handler.postDelayed({ Toast.makeText(context, "일일미션 투표하기 완료(${count+1}회)", Toast.LENGTH_SHORT).show() }, 0)
                        Log.d("homeApi", "${theVotes.statusCode} ${theVotes.message}")
                        handleResponse(theVotes)
                    } else {
                        handler.postDelayed({ Toast.makeText(context, "일일미션 투표하기 실패(${count+1}회)", Toast.LENGTH_SHORT).show() }, 0)
                        handleError("Response body is null.")
                    }
                } else {
                    handler.postDelayed({ Toast.makeText(context, "일일미션 투표하기 실패(${count+1}회)", Toast.LENGTH_SHORT).show() }, 0)
                    handleError("homeApi Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleResponse(theVotes: ServerResponse<NumberOfVote>?) {
        VoteMissionDB.init(context)
        val editor = VoteMissionDB.getInstance().edit()
        if (theVotes != null) {
            theVotes.data.voteCount?.let { voteMissionListener.giveVote(it) } // 리스너 호출

            editor.putInt("vote", 1)
            editor.apply()
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("투표하기 미션 api Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        ServiceAccessTokenDB.init(context)
        val serviceTokenDB = ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }

    // voteCount 는 투표할 투표권 개수를 뜻함
    private suspend fun getPopupVoteApi(voteCount: Int?) {
        Log.d("getPopupVoteApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        val voteRequest = NumberOfVote(voteCount = voteCount)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<PopupVoteData>> = withContext(Dispatchers.IO) {
                    PopupVoteInstance.getCurrentVoteInfoService().getCurrentVoteInfo(accessToken, voteRequest)
                }
                if (response.isSuccessful) {
                    val voteInfo: ServerResponse<PopupVoteData>? = response.body()
                    if (voteInfo != null) {
                        Log.d("getPopupVoteApi 함수", "${voteInfo.statusCode} ${voteInfo.message}")
                        // UI 업데이트는 Main 스레드에서 진행
                        withContext(Dispatchers.Main) {
                            handlePopupVote(voteInfo)
                        }
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("getPopupVoteApi 함수 Error: ${response.code()}-${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handlePopupVote(voteInfo: ServerResponse<PopupVoteData>) {
        Log.d("handlePopupVote 함수","호출 성공")
        binding.tvVotes.text = voteInfo.data.voteCount.toString()
        binding.tvVotes2.text = voteInfo.data.voteCount.toString()
        binding.tvArtistName.text = voteInfo.data.artistName // 아티스트 이름
        binding.tvArtistName2.text = voteInfo.data.artistName
        binding.tvMonth.text = voteInfo.data.month.toString() // 월
        binding.tvPercent.text = voteInfo.data.rank.toString() + "%" // 비율
    }
}
