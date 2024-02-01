package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teuniverse.databinding.FragmentCalendarBinding
import com.example.teuniverse.databinding.FragmentCommunityDetailBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.Calendar

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var scheduleList: ArrayList<CalendarItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        scheduleList = ArrayList()

        // 리사이클러뷰 어댑터 연결
        calendarAdapter = CalendarAdapter(scheduleList)

        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.month - 1, date.day)

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY) {
                // 토요일인 경우 파란색으로 설정
                widget.setDateTextAppearance(R.style.CalendarDay_Saturday)
            } else if (dayOfWeek == Calendar.SUNDAY) {
                // 일요일인 경우 빨간색으로 설정
                widget.setDateTextAppearance(R.style.CalendarDay_Sunday)
            } else {
                // 그 외의 날짜는 기본 색상으로 설정
                widget.setDateTextAppearance(R.style.CalendarDay_Default)
            }
        }
        return binding.root
    }

}