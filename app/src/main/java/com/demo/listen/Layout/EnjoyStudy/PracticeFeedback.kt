package com.demo.listen.Layout.EnjoyStudy

import android.content.Intent
import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.Adapter.FeedbackAdapter
import com.demo.listen.Layout.DataType.FeedbackItem
import com.demo.listen.MainActivity
import com.demo.listen.R

class PracticeFeedback : AppCompatActivity() {

    private lateinit var feedbackList: RecyclerView
    private lateinit var adapter: FeedbackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@PracticeFeedback,
                    MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        })

        setContentView(R.layout.activity_practice_feedback)

        initList()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getData(): List<FeedbackItem> {
        // TODO: 获取练习数据

        // test
        return listOf(
            FeedbackItem("b","第1次练习得分","85"),
            FeedbackItem("ch","第2次练习得分","92"),
            FeedbackItem("h","第3次练习得分","78"),
            FeedbackItem("y","第4次练习得分","95"),
        )
    }

    private fun initList() {
        feedbackList = findViewById<RecyclerView>(R.id.gl_feedback_list)
        feedbackList.layoutManager = GridLayoutManager(this, 2)
        adapter = FeedbackAdapter(getData())
        feedbackList.adapter = adapter
    }
}