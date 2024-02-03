package com.example.teuniverse

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        customCalendar()
        motionCalendar()

        return binding.root
    }

    // 달력 커스텀 모음
    private fun customCalendar() {
        // Decorator 추가
        binding.calendarView.addDecorators(SundayDecorator(), DayDecorator(requireContext()),
            TodayDecorator(requireContext()))
        // 폰트 사이즈
        binding.calendarView.setDateTextAppearance(R.style.CustomDateTextAppearance)
        binding.calendarView.setWeekDayTextAppearance(R.style.CustomWeekDayAppearance)
        binding.calendarView.setHeaderTextAppearance(R.style.CustomHeaderTextAppearance)
    }
    // 달력 기능 모음
    private fun motionCalendar() {
        // 월 변경 이벤트 리스너 설정
//        binding.calendarView.setOnMonthChangedListener { _, date ->
//            // 월이 변경되면 해당 년도로 다시 설정
//            if (date.year != currentYear) {
//                initializeCalendar(binding.calendarView)
//            }
//        }
    }


    // 일요일에 해당하는 날짜 색 변경
    inner class SundayDecorator : DayViewDecorator {
        private val calendar = Calendar.getInstance()
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar) // 주어진 캘린더 인스턴스에 오늘의 정보를 복사
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK) // 일주일을 받아옴
            return weekDay == Calendar.SUNDAY // 그중 일요일을 리턴
        }
        override fun decorate(view: DayViewFacade?) {
            // 하루(일요일)의 전체 텍스트에 범위의 색 추가
            view?.addSpan(object : ForegroundColorSpan(Color.RED){})
        }
    }

    /* 선택된 날짜의 background를 설정하는 클래스 */
    private inner class DayDecorator(context: Context) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context,R.drawable.calendar_selector)
        // true를 리턴 시 모든 요일에 내가 설정한 드로어블이 적용된다
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return true
        }
        // 일자 선택 시 내가 정의한 드로어블이 적용되도록 한다
        override fun decorate(view: DayViewFacade) {
            view.setSelectionDrawable(drawable!!)
        }
    }

    /* 오늘 날짜의 background를 설정하는 클래스 */
    private class TodayDecorator(context: Context): DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context,R.drawable.custom_circle_mp)
        private var date = CalendarDay.today() // 오늘 날짜
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day?.equals(date)!!
        }
        override fun decorate(view: DayViewFacade?) {
            view?.setBackgroundDrawable(drawable!!)
        }
    }
}



