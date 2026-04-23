package com.demo.listen.Layout.EnjoyStudy

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R

class SyllablePractice : AppCompatActivity() {

    private var syllable: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_syllable_practice)

        syllable = intent.getStringExtra("Syllable")
        findViewById<TextView>(R.id.tv_syllable_target).text = syllable
        addPinYin()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    // 所有 b 开头的拼音组合（常用）
    val bPinyinList = listOf(
        "ba", "bo", "bi", "bu",
        "bai", "bei", "bao", "ban", "ben",
        "bang", "beng", "bian", "biao", "bie",
        "bin", "bing"
    )

    private fun addPinYin() {
        val gridLayout = findViewById<GridLayout>(R.id.gl_syllable_group)

        gridLayout.removeAllViews()
        gridLayout.columnCount = 3  // 3 列

        for ((index, pinyin) in bPinyinList.withIndex()) {
            // 创建 TextView
            val textView = TextView(this).apply {
                text = pinyin
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(16, 12, 16, 12)

                setBackgroundResource(R.drawable.input_box)
                setTextColor(Color.BLACK)
            }

            // 创建 GridLayout.LayoutParams
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)

                // 设置边距
                setMargins(8, 8, 8, 8)
            }

            gridLayout.addView(textView, params)
        }
    }
}