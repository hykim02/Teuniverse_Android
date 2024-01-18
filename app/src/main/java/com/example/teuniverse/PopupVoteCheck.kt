import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.teuniverse.databinding.PopupVoteCheckBinding

class PopupVoteCheck(
    context: Context,
    private val voteCount: String?,
    private val artistName: String?,
    private val month: String?,
    private val rank: String?,
    private val okCallback: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: PopupVoteCheckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 팝업창 띄움
        binding = PopupVoteCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        okCallback("$voteCount $artistName $month $rank")
        binding.tvVoteCount.text = voteCount
        binding.tvMonth.text = month
        binding.tvArtistName.text = artistName
        binding.tvVoteCount2.text = voteCount
        binding.tvArtistName2.text = artistName
        binding.tvPercent.text = rank

        // Now you can use the data directly
        binding.bntOk.setOnClickListener {
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
}
