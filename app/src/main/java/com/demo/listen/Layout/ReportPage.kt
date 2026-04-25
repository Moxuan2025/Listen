package com.demo.listen.Layout

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R
import org.w3c.dom.Text

class ReportPage : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var score: TextView
    private lateinit var btBack: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_page)

        mapWidget()
        initPage()
        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        image = findViewById<ImageView>(R.id.report_image)
        score = findViewById<TextView>(R.id.report_score)
        btBack = findViewById<TextView>(R.id.report_back)
    }

    private fun initPage() {
        // TODO: 获取评估图片并加载到 image

        // TODO: 获取综合评分并显示
        score.text = "综合评分: " + 96
    }

    private fun handleClick() {
        btBack.setOnClickListener {
            // TODO: 跳转到正确的界面
            finish()
        }
    }
}