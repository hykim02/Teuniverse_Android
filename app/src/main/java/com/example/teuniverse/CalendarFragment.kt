package com.example.teuniverse

import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teuniverse.databinding.FragmentCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.Calendar

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var scheduleList: ArrayList<CalendarItem>
    private lateinit var currentYear: Integer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        scheduleList = ArrayList()
        // 리사이클러뷰 어댑터 연결
        calendarAdapter = CalendarAdapter(scheduleList)

        // SundayDecorator를 추가
        binding.calendarView.addDecorator(SundayDecorator())
        // 현재 날짜를 가져와서 해당 년도를 가져옴
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).let { it } // Nullable 타입을 사용하여 초기화

        // 월 변경 이벤트 리스너 설정
        binding.calendarView.setOnMonthChangedListener { _, date ->
            // 월이 변경되면 해당 년도로 다시 설정
            if (date.year != currentYear) {
                initializeCalendar(binding.calendarView)
            }
        }
        return binding.root
    }

    // 일요일에 해당하는 날짜 색 변경
    inner class SundayDecorator() : DayViewDecorator {
        private val calendar = Calendar.getInstance()
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            // 주어진 캘린더 인스턴스에 오늘의 정보를 복사
            day?.copyTo(calendar)
            // 일주일을 받아옴
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
            // 그중 일요일을 리턴
            return weekDay == Calendar.SUNDAY
        }

        override fun decorate(view: DayViewFacade?) {
            // 하루(일요일)의 전체 텍스트에 범위의 색 추가
            view?.addSpan(object : ForegroundColorSpan(Color.RED){})
        }
    }

    private fun initializeCalendar(calendarView: MaterialCalendarView) {
        // Nullable 타입을 사용했기 때문에 null 체크를 해줘야 함
        if (::currentYear.isInitialized) {
            // 특정 년도로 초기화
            calendarView.setCurrentDate(CalendarDay.from(currentYear.toInt(), 1, 1), true)
            calendarView.setSelectedDate(CalendarDay.today())  // 현재 날짜를 선택하도록 설정

            // 현재 날짜의 스타일 설정
            val currentDayDecorator = CurrentDayDecorator()
            calendarView.addDecorator(currentDayDecorator)
        }
    }

    // 현재 날짜 커스텀
    inner class CurrentDayDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            // 현재 날짜와 같으면 스타일을 적용
            return day == CalendarDay.today()
        }
        override fun decorate(view: DayViewFacade?) {
            // 현재 날짜의 텍스트 색상을 빨간색으로 변경
            view?.addSpan(object : ForegroundColorSpan(Color.WHITE) {})

            // 동그란 이미지를 배경으로 설정
            context?.resources?.getDrawable(R.drawable.day_circle)
                ?.let { view?.setBackgroundDrawable(it) }
        }
    }
}


