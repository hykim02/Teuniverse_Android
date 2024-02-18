package com.example.teuniverse

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.teuniverse.databinding.FragmentCalendarBinding
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import retrofit2.Response
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.HashMap

class CalendarFragment : Fragment(), PopupScheduleType.CommunicationListener {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var scheduleList: ArrayList<Event>
    private lateinit var datesWithDots: MutableList<CalendarDay>

    // 전역 변수로 선언
    private var currentYear: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        // 현재 날짜를 가져와서 해당 년도를 가져옴
        currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // 초반에 한 번만 호출되도록 조건 설정 필요
//        callApi(currentYear)

        datesWithDots = mutableListOf()

        scheduleList = ArrayList()
        // 리사이클러뷰 어댑터 연결gi
        calendarAdapter = CalendarAdapter(scheduleList)

        binding.imgBtnPerson.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_calendar_to_profileFragment)
        }

        binding.typeBtn.setOnClickListener {
            showPopupScheduleTypeDialog()
        }

        binding.imgBtnVote.setOnClickListener {
            showPopupMissionDialog()
        }

        customCalendar()
        motionCalendar()

        findDate()
        setDot(requireContext(), datesWithDots)
//        makeDot(datesWithDots)
        makeSchedule()

        lifecycleScope.launch {
            getNumberOfVotes()
        }

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
        binding.calendarView.addDecorators(SunDecorator(), TodayDecorator(requireContext()))
        // 폰트 사이즈
        binding.calendarView.setDateTextAppearance(R.style.CustomDateTextAppearance)
        binding.calendarView.setWeekDayTextAppearance(R.style.CustomWeekDayAppearance)
        binding.calendarView.setHeaderTextAppearance(R.style.CustomHeaderTextAppearance)
    }

    // 달력 기능 모음
    private fun motionCalendar() {
        // 월 변경 이벤트 리스너 설정
        binding.calendarView.setOnMonthChangedListener { _, date ->
            binding.calendarRv.visibility = GONE
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

    // 일정있는 날 찾기 (일정 개수만큼 점 조절 필요)
    private fun findDate() {
        MonthDBManager.initAll(requireContext())
        for (i in 1..12) {
            val monthDB = MonthDBManager.getMonthInstance(i)
            val isExist = MonthDBManager.doesSharedPreferencesFileExist(requireContext(), i.toString())

            if (isExist) {
                val newDates = monthDB.all.keys.mapNotNull { key ->
                    val dateParts = key.split("-")
                    if (dateParts.size == 3) {
                        val year = dateParts[0].toInt()
                        val month = dateParts[1].toInt()-1
                        val day = dateParts[2].toInt()
                        CalendarDay.from(year, month, day)
                    } else {
                        null
                    }
                }
                // 점 표시할 날짜리스트
                datesWithDots.addAll(newDates)
            }
        }
    }

    // 점 색상 추가하고 점 그리기
//    private fun makeDot(datesWithDots: MutableList<CalendarDay>) {
//        // 동그라미를 표시하는 데코레이터를 모두 제거
//        binding.calendarView.removeDecorators()
//
//        // 특정 날짜에 한 줄로 나란히 표시할 4개의 점의 색상
//        val colorList = ArrayList<Int>()
//        context?.let { setDot(it, colorList, datesWithDots) } // 색 추가
//        Log.d("최종 colorList", colorList.toString())
//
//        // DayViewDecorator를 사용하여 날짜에 점을 표시합니다.
//        binding.calendarView.addDecorator(object : DayViewDecorator {
//            override fun shouldDecorate(day: CalendarDay?): Boolean {
//                return datesWithDots.contains(day)
//            }
//
//            override fun decorate(view: DayViewFacade?) {
//                // 날짜에 표시할 한 줄로 나란히 표시할 4개의 점을 설정
//                view?.addSpan(MultiColorDotSpan(8f, colorList))
//            }
//        })
//    }


    // 필터링된 스케줄 타입에 맞는 색상 추가
    private fun setDot(context: Context, datesWithDots: MutableList<CalendarDay>) {
        // 동그라미를 표시하는 데코레이터를 모두 제거
        binding.calendarView.removeDecorators()
        // 특정 날짜에 한 줄로 나란히 표시할 4개의 점의 색상
        val colorList = ArrayList<Int>()

        ScheduleTypeDB.init(requireContext())
        val typeDB = ScheduleTypeDB.getInstance().all
        val editor = ScheduleTypeDB.getInstance().edit()
        val isTrueList = arrayListOf<String>()

        val sharedPrefsFile = File("${context.filesDir.parent}/shared_prefs/ScheduleType.xml")
        val isExist = sharedPrefsFile.exists()
        // 스케줄 타입 DB가 존재한다면
        if (isExist) {
            datesWithDots.forEach { date ->
                isTrueList.clear()
                if (typeDB.getValue("video") == true) {
                    Log.d("video", date.toString())
                    isTrueList.add("방송")
                }

                if (typeDB.getValue("festival") == true) {
                    Log.d("festival", date.toString())
                    isTrueList.add("행사")
                }

                if (typeDB.getValue("cake") == true) {
                    Log.d("cake", date.toString())
                    isTrueList.add("기념일")
                }

                if (typeDB.getValue("more") == true) {
                    Log.d("more", date.toString())
                    isTrueList.add("기타")
                }
                Log.d("isTrueList", isTrueList.toString())
                isExistSchedule(isTrueList, colorList, date)
            }
        } else { // 존재하지 않는다면
            datesWithDots.forEach { date ->
                isTrueList.add("방송")
                isTrueList.add("행사")
                isTrueList.add("기념일")
                isTrueList.add("기타")
                isExistSchedule(isTrueList, colorList, date)
            }

            editor.putBoolean("video", true)
            editor.putBoolean("festival", true)
            editor.putBoolean("cake", true)
            editor.putBoolean("more", true)
            editor.apply()
        }
    }

    // 스케줄 타입이 true인 것들 중에 일정이 존재하는지
    private fun isExistSchedule(isTrueList: ArrayList<String>, list: ArrayList<Int>, date: CalendarDay) {
        val year = date.year
        val month = date.month + 1 // 월은 0부터 시작하므로 1을 더해줌
        val formatMonth = formatNum(month)
        val day2 = date.day
        val formatDay = formatNum(day2)
        val key = "$year-$formatMonth-$formatDay"
        list.clear()

        MonthDBManager.initMonth(requireContext(), month)
        val monthDB = MonthDBManager.getMonthInstance(month)
        val isExist = MonthDBManager.doesSharedPreferencesFileExist(requireContext(), month.toString())

        if (isExist) {
            Log.d("isExist", month.toString())
            // 특정 키에 대한 값을 가져오기
            val dataString = monthDB.getString(key, null)
            Log.d("dataString2", dataString.toString())

            if (dataString != null) {
                val jsonArray = JSONArray(dataString)
                // JSONArray 내 각 객체를 순회하면서 데이터 추출
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val type = jsonObject.getString("type")
                    if (isTrueList.contains(type)) {
                        Log.d("일치 type", type.toString())
                        findCorrectColor(type, list)
                    }
                }
                Log.d("colorList", list.toString())
                // DayViewDecorator를 사용하여 날짜에 점을 표시합니다.
                binding.calendarView.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay?): Boolean {
                        // 특정 날짜에만 점을 표시하도록 수정
                        return day != null && day.year == year && day.month + 1 == month && day.day == day2
                    }

                    override fun decorate(view: DayViewFacade?) {
                        // 날짜에 표시할 한 줄로 나란히 표시할 4개의 점을 설정
                        view?.addSpan(MultiColorDotSpan(8f, list))
                    }
                })
            }
        }
    }

    // 타입에 맞는 색상 찾기
    private fun findCorrectColor(type: String, list: ArrayList<Int>) {
        Log.d("findCorrectColor", "실행")
        if (type == "기념일") {
            list.add(Color.parseColor("#FF5900"))
        } else if (type == "방송") {
            list.add(Color.parseColor("#4BCEFA"))
        } else if (type == "행사") {
            list.add(Color.parseColor("#20E02A"))
        } else {
            list.add(Color.parseColor("#F9D400"))
        }
    }

    // 점 그리기 클래스
    private inner class MultiColorDotSpan(private val radius: Float, private val colors: ArrayList<Int>) : LineBackgroundSpan {
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
            val spacing = 9f
            Log.d("multicolorSpan","실행")
            for (i in colors.indices) {
                val cx = left + i * (2 * radius + spacing) + 40
                val cy = bottom + radius + 20 // 텍스트 아래에 점을 그리도록 계산
                paint.color = colors[i]
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
    }

    // db에서 데이터 들고와 일정 보여주는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeSchedule() {
        binding.calendarView.setOnDateChangedListener { _, date, selected ->
            // 날짜 클릭 이벤트 처리
            if (selected) {
                scheduleList.clear()
                // 클릭된 날짜의 이벤트를 표시하는 로직을 여기에 추가
                val selectedYear = date.year
                val selectedMonth = date.month + 1 // 월은 0부터 시작하므로 1을 더해줌
                val formatMonth = formatNum(selectedMonth)
                val selectedDay = date.day
                val formatDay = formatNum(selectedDay)

                val key = "$selectedYear-$formatMonth-$formatDay"
                // 해당 월에 해당하는 DB 파일을 찾아 데이터 가져오기
                val dbFileName = "$selectedMonth"
                val isExist = MonthDBManager.doesSharedPreferencesFileExist(requireContext(), dbFileName)

                ScheduleTypeDB.init(requireContext())
                val typeDB = ScheduleTypeDB.getInstance().all
                // value가 true인 것이 하나라도 존재한다면
                if (typeDB.containsValue(true)) {
                    getScheduleData(dbFileName, isExist, key)
                }
            }
        }
    }

    // DB 파일이 존재할 경우 데이터를 가져오는 작업을 수행
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getScheduleData(dbFileName: String, isExist: Boolean, key: String) {
        if (isExist) {
            val monthDB = MonthDBManager.getMonthInstance(dbFileName.toInt())
            // 특정 키에 대한 값을 가져오기
            val dataString = monthDB.getString(key, null)
            Log.d("dataString", dataString.toString())

            if (dataString != null) {
                binding.calendarRv.visibility = View.VISIBLE
                // 가져온 데이터가 null이 아니라면 JSON 형식의 문자열이므로 파싱하여 사용
                val jsonArray = JSONArray(dataString)
                // JSONArray 내 각 객체를 순회하면서 데이터 추출
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val content = jsonObject.getString("content")
                    val startAt = setTime(jsonObject.getString("startAt"))
                    val type = jsonObject.getString("type")

                    findCorrectData(content, type, startAt)
                }
            } else {
                // 가져온 데이터가 null일 경우에 대한 처리
                scheduleList.clear()
                binding.calendarRv.visibility = GONE
            }
        } else {
            // DB 파일이 존재하지 않을 경우 처리
            // 원하는 작업을 수행하거나 메시지를 표시할 수 있습니다.
        }
    }

    // 표시된 동그라미와 일치하는 데이터 찾기
    private fun findCorrectData(content: String, type: String, startAt: String) {
        ScheduleTypeDB.init(requireContext())
        val typeDB = ScheduleTypeDB.getInstance().all
        val typeList = arrayListOf<String>()
        for ((key, value) in typeDB.entries) {
            if (value == true) {
                typeList.add(key)
            }
        }

        for (item in typeList.indices) {
            if (typeList[item] == "video" && type == "방송") {
                val typeImg = setTypeImg(type)
                scheduleList.add(Event(content, typeImg.toString(), startAt))
            } else if (typeList[item] == "festival" && type == "행사") {
                val typeImg = setTypeImg(type)
                scheduleList.add(Event(content, typeImg.toString(), startAt))
            } else if (typeList[item] == "cake" && type == "기념일") {
                val typeImg = setTypeImg(type)
                scheduleList.add(Event(content, typeImg.toString(), startAt))
            } else if (typeList[item] == "more" && type == "기타") {
                val typeImg = setTypeImg(type)
                scheduleList.add(Event(content, typeImg.toString(), startAt))
            } else {
                continue
            }
        }
        Log.d("scheduleList", scheduleList.toString())

        if (scheduleList.size >= 1) {
            val spanCount = scheduleList.size
            val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
            binding.calendarRv.adapter = calendarAdapter
            binding.calendarRv.layoutManager = layoutManager

            // 어댑터에 데이터가 변경되었음을 알리기
            calendarAdapter.notifyDataSetChanged()
        } else {
            binding.calendarRv.visibility = GONE
        }
    }

    // 투표권 개수 가져오기
    private suspend fun getNumberOfVotes() {
        Log.d("getNumberOfVotes 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(Dispatchers.IO) {
                    VoteCountInstance.getVotesService().getVotes(accessToken)
                }
                if (response.isSuccessful) {
                    val theVote: ServerResponse<NumberOfVote>? = response.body()
                    if (theVote != null) {
                        Log.d("response", "${theVote.statusCode} ${theVote.message}")
                        handleTheVotes(theVote)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("getNumverOfVotes함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 보유 투표권 개수 조회
    private fun handleTheVotes(votes: ServerResponse<NumberOfVote>) {
        Log.d("handleTheVotes 함수","호출 성공" )
        binding.voteCount.text = votes.data.voteCount.toString()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }

    // 일정 시간 추출 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTime(time: String): String {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(time, formatter)

        // LocalDateTime에서 시간과 분 추출
        val hour = dateTime.hour.toString()
        val minute = dateTime.minute.toString()

        // 추출된 시간과 분을 2자리 숫자로 변환하여 변수에 저장
        val extractedHour = String.format("%02d", hour.toInt())
        val extractedMinute = String.format("%02d", minute.toInt())

        return "$extractedHour:$extractedMinute"
    }

    private fun setTypeImg(type: String): Int {
        if (type == "기념일") {
            return R.drawable.cake_on
        } else if (type == "행사") {
            return R.drawable.festival_on
        } else if (type == "방송") {
            return R.drawable.video_on
        } else { // 기타
            return R.drawable.more_on
        }
    }

    // 날짜 포맷 함수
    private fun formatNum(num: Int): String {
        return if (num < 10) {
            "0$num"
        } else {
            num.toString()
        }
    }

    /* api 관련 함수 */
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
            Log.d("일정api", response.data.toString())
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

    // 스케줄 타입 다이얼로그를 생성
    private fun showPopupScheduleTypeDialog() {
        val popupScheduleType = PopupScheduleType(requireContext(), this)
        popupScheduleType.setListener(this)
        popupScheduleType.show()

        popupScheduleType.setOnDismissListener {
            Log.d("YourActivityOrFragment", "PopupScheduleType dialog dismissed.")
        }
    }

    // CommunicationListener의 메서드 구현
    override fun onPopupCompleteButtonClicked() {
        // 팝업에서 버튼이 클릭되었을 때 실행할 코드
        Log.d("팝업창 완료","click")
        setDot(requireContext(), datesWithDots)
    }

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
    }
}



