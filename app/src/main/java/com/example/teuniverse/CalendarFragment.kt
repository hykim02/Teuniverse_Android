package com.example.teuniverse

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.text.style.ReplacementSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.teuniverse.databinding.FragmentCalendarBinding
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Calendar

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var scheduleList: ArrayList<CalendarItem>

    // 전역 변수로 선언
    private var currentYear: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        scheduleList = ArrayList()
        // 리사이클러뷰 어댑터 연결
        calendarAdapter = CalendarAdapter(scheduleList)

        // 현재 날짜를 가져와서 해당 년도를 가져옴
        currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // 초반에 한 번만 호출되도록 조건 설정 필요
//        callApi(currentYear)

        customCalendar()
        motionCalendar()
        makeDot()

        return binding.root
    }

    // 1-12월까지 api 모두 호출
    private fun callApi(year: Int) {
        for (i in 1..12) {
            lifecycleScope.launch {
                scheduleApi(year, i)
            }
        }
    }

    // 달력 커스텀 모음
    private fun customCalendar() {
        // Decorator 추가
        binding.calendarView.addDecorators(SunDecorator(),
            TodayDecorator(requireContext()))
        // 폰트 사이즈
        binding.calendarView.setDateTextAppearance(R.style.CustomDateTextAppearance)
        binding.calendarView.setWeekDayTextAppearance(R.style.CustomWeekDayAppearance)
        binding.calendarView.setHeaderTextAppearance(R.style.CustomHeaderTextAppearance)
    }
    // 달력 기능 모음
    private fun motionCalendar() {
        // 월 변경 이벤트 리스너 설정
        binding.calendarView.setOnMonthChangedListener { _, date ->
            // 월이 변경되면 해당 년도로 다시 설정
            if (date.year != currentYear) {
                initializeCalendar(binding.calendarView)
            }
        }
    }

    // 해당 년도만 볼 수 있도록
    private fun initializeCalendar(calendarView: MaterialCalendarView) {
        // 특정 년도로 초기화
        calendarView.setCurrentDate(CalendarDay.from(currentYear, 1, 1), true)
        calendarView.selectedDate = CalendarDay.today()  // 현재 날짜를 선택하도록 설정
    }

    /* 일요일 날짜의 색상을 설정하는 클래스 */
    private inner class SunDecorator: DayViewDecorator {
        private val calendar = Calendar.getInstance()
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)
            val weekDay = calendar[Calendar.DAY_OF_WEEK]
            return weekDay == Calendar.SUNDAY
        }
        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(ForegroundColorSpan(Color.RED))
        }
    }

    /* 오늘 날짜의 background를 설정하는 클래스 */
    private inner class TodayDecorator(context: Context): DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context,R.drawable.custom_circle_mp)
        private var today = CalendarDay.today() // 오늘 날짜(0 = 1월)

        // decorate 여부 결정 함수 (true -> decorate 함수 호출)
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day?.equals(today)!!
        }
        override fun decorate(view: DayViewFacade?) {
            view?.setBackgroundDrawable(drawable!!)
        }
    }

    private fun makeDot() {
        // 특정 날짜에 한 줄로 나란히 표시할 4개의 점의 색상
        val colors = intArrayOf(
            Color.parseColor("#4BCEFA"),
            Color.parseColor("#20E02A"),
            Color.parseColor("#FF5900"),
            Color.parseColor("#F9D400"))

        // 특정 날짜에 표시할 날짜 리스트 생성
        val datesWithDots = listOf(
            CalendarDay.from(2024, 1, 10),
            CalendarDay.from(2024, 1, 15),
            CalendarDay.from(2024, 1, 20),
        )

        // DayViewDecorator를 사용하여 날짜에 점을 표시합니다.
        binding.calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay?): Boolean {
                return datesWithDots.contains(day)
            }

            override fun decorate(view: DayViewFacade?) {
                // 날짜에 표시할 한 줄로 나란히 표시할 4개의 점을 설정
                view?.addSpan(MultiColorDotSpan(8f, colors))
            }
        })
    }

    // 점 표시 클래스
    private inner class MultiColorDotSpan(private val radius: Float, private val colors: IntArray) : LineBackgroundSpan {
        override fun drawBackground(
            canvas: Canvas,
            paint: Paint,
            left: Int,
            right: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            lineNumber: Int
        ) {
            val spacing = 7f

            for (i in colors.indices) {
                val cx = left + i * (2 * radius + spacing) + 42
                val cy = bottom + radius + 20 // 텍스트 아래에 점을 그리도록 계산
                paint.color = colors[i]
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
    }

    // 1-12월 일정 받아오기
    private suspend fun scheduleApi(year: Int, month: Int) {
        Log.d("scheduleApi $month", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<EventResponse> = withContext(
                    Dispatchers.IO) {
                    CalendarInstance.scheduleService().getSchedule(year, month, accessToken)
                }
                if (response.isSuccessful) {
                    val theSchedule: EventResponse? = response.body()
                    if (theSchedule != null) {
                        Log.d("scheduleApi $month", "${theSchedule.statusCode} ${theSchedule.message}")
                        handleResponse(theSchedule, month)
                    } else {
                        handleError("Response body is null.",month)
                    }
                } else {
                    handleError("scheduleApi $month Error: ${response.code()} - ${response.message()}",month)
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.",month)
        }
    }

    private fun handleResponse(response: EventResponse, month: Int) {
        MonthDBManager.initMonth(requireContext(), month) // 초기화
        val monthDB = MonthDBManager.getMonthInstance(month) // 객체 얻기

        // DB에 데이터 저장
        if (response.data.isNotEmpty()) {
            response.data.forEach { (date, events) ->
                val jsonString = Gson().toJson(events) // Convert events List to JSON String
                with(monthDB.edit()) {
                    putString(date, jsonString)
                    apply()
                }
            }
        }
    }

    private fun handleError(errorMessage: String, month: Int) {
        // 에러를 처리하는 코드
        Log.d("일정Api $month Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        MainActivity.ServiceAccessTokenDB.init(requireContext())
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



