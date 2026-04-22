package com.demo.listen.Layout.Assessment

import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R

class PracticeFeedback : AppCompatActivity() {

    private lateinit var feedbackList: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practice_feedback)

        feedbackList = findViewById<GridLayout>(R.id.gl_feedback_list)
//        loadFeedbackHistory()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadFeedbackHistory() {
        // 测试数据：练习次数和对应的分数
        val feedbackData = listOf(
            "第一次练习得分" to 100,
            "第二次练习得分" to 85,
            "第三次练习得分" to 92,
            "第四次练习得分" to 78
        )

        // 动态添加 FeedbackItem
        feedbackData.forEach { (timesText, score) ->
            addFeedbackItem(feedbackList, timesText, score)
        }
    }

    private fun addFeedbackItem(gridLayout: GridLayout, timesText: String, score: Int) {
        // TODO: get data
        // 加载 fragment XML 布局
        val itemView = layoutInflater.inflate(R.layout.fragment_feedback_item, gridLayout, false)

        val tvFeedbackTimes = itemView.findViewById<TextView>(R.id.tv_feedback_times)
        val tvFeedbackScore = itemView.findViewById<TextView>(R.id.tv_feedback_score)

        // 设置数据
        tvFeedbackTimes.text = timesText
        tvFeedbackScore.text = score.toString()

        // 设置 GridLayout 布局参数（每项占一列）
        val params = GridLayout.LayoutParams().apply {
            width = 0  // 使用权重分配宽度
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)  // 每项占一列，权重为1
            setMargins(4, 0, 4, 4)
        }
        itemView.layoutParams = params

        // 添加到 GridLayout
        gridLayout.addView(itemView)
    }
}