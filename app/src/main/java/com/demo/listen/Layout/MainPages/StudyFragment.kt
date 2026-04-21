package com.demo.listen.Layout.MainPages

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.demo.listen.R
import java.time.LocalDate
import java.time.YearMonth


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var gridLayout: GridLayout
    private lateinit var tvMonth: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    private val today = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    private var curYear = today.year
    @RequiresApi(Build.VERSION_CODES.O)
    private var curMonth: Int = today.monthValue
    @RequiresApi(Build.VERSION_CODES.O)
    private val curDay: String = today.dayOfMonth.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study, container, false)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridLayout = requireView().findViewById<GridLayout>(R.id.gl_calendar)
        tvMonth = requireView().findViewById<TextView>(R.id.tv_month)

        handleClick()
        setCalender(curYear, curMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleClick() {
        val  v = requireView()
        v.findViewById<Button>(R.id.btn_pre_month).setOnClickListener { // 上个月
            if (--curMonth < 1) {
                curMonth = 12
                setCalender(--curYear, curMonth)
            } else
                setCalender(curYear, curMonth)
        }
        v.findViewById<Button>(R.id.btn_nxt_month).setOnClickListener { // 下个月
            if (++curMonth > 12) {
                curMonth = 1
                setCalender(++curYear, curMonth)
            } else
                setCalender(curYear, curMonth)
        }
        v.findViewById<Button>(R.id.btn_clockin).setOnClickListener { // 言语/听力评估
            val result = Bundle().apply {
                putString("event", "ClockIn")
            }
            parentFragmentManager.setFragmentResult("Study", result)
        }
        v.findViewById<Button>(R.id.btn_personal_assessment).setOnClickListener { // 言语/听力学习
            val result = Bundle().apply {
                putString("event", "PersonalAssessment")
            }
            parentFragmentManager.setFragmentResult("Study", result)
        }
        v.findViewById<Button>(R.id.btn_speak_learn).setOnClickListener { // 情景对话
            val result = Bundle().apply {
                putString("event", "SpeakStudy")
            }
            parentFragmentManager.setFragmentResult("Study", result)
        }
    }

    data class MonthCalendar(
        val weeks: List<List<Int?>>,
        val weekDays: List<String>
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCalender(year: Int, month: Int) {
        setMonthTitle()
        var calendar = getMonthCalendarWithHeaders(year, month)

        gridLayout.columnCount = 7
        gridLayout.removeAllViews()

        // 1. 先添加星期标题行
        calendar.weekDays.forEach { dayName ->
            val textView = createTextView(gridLayout.context, dayName)
            gridLayout.addView(textView)
        }
        // 2. 添加日期行
        calendar.weeks.forEach { week ->
            week.forEach { date ->
                val textView = createTextView(
                    gridLayout.context,
                    date?.toString() ?: "",
                )
                // 设置点击事件

                gridLayout.addView(textView)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthCalendarWithHeaders(year: Int = curYear,
                                    month: Int = curMonth): MonthCalendar {
        val yearMonth = YearMonth.of(year, month)
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()

        // 获取第一天是周几（周日=7, 周一=1, ..., 周六=6）
        // 转换为周日=1, 周一=2, ..., 周六=7 的格式
        val firstDayOfWeek = when (firstDay.dayOfWeek.value) {
            7 -> 1  // 周日为第1天
            else -> firstDay.dayOfWeek.value + 1  // 周一=2, 周二=3, ..., 周六=7
        }

        val weeks = mutableListOf<List<Int?>>()
        var currentWeek = mutableListOf<Int?>()

        // 填充第一周的空格（周日为第一天）
        repeat(firstDayOfWeek - 1) {
            currentWeek.add(null)
        }

        // 填充日期
        for (day in 1..lastDay.dayOfMonth) {
            currentWeek.add(day)
            if (currentWeek.size == 7) {
                weeks.add(currentWeek.toList())
                currentWeek.clear()
            }
        }

        // 填充最后一周的剩余部分
        if (currentWeek.isNotEmpty()) {
            repeat(7 - currentWeek.size) {
                currentWeek.add(null)
            }
            weeks.add(currentWeek)
        }

        // 星期标题（周日为第一天）
        val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

        return MonthCalendar(weeks, weekDays)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTextView(
        context: Context,
        text: String,
    ): TextView {
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)

            if (text.isEmpty()) {
                // 非当前月的日期（空白）
                setTextColor(ContextCompat.getColor(context, android.R.color.transparent))
                isEnabled = false
            } else {
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                textSize = 16f
            }

            if (text == curDay)
                setBackgroundResource(R.drawable.orange_circle_bg)

            // 设置布局参数
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
        }
    }

    private var monthList: Array<String> = arrayOf("一月", "二月", "三月", "四月", "五月", "六月",
                                        "七月", "八月", "九月", "十月", "十一月", "十二月", )
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthTitle() {
        tvMonth.text = monthList[curMonth-1]
    }

}