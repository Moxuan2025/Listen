package com.demo.listen.Layout.EnjoyStudy

import android.content.Intent
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
import kotlin.jvm.java

class SyllablePractice : AppCompatActivity() {

    private var syllable: String? = null

    private var pinyin: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_syllable_practice)

        syllable = intent.getStringExtra("Syllable")
        pinyin = intent.getStringArrayListExtra("Pinyin") ?: emptyList()

        findViewById<TextView>(R.id.tv_syllable_target).text = syllable
        addPinYin(pinyin ?: emptyList())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun addPinYin(list: List<String>) {
        val gridLayout = findViewById<GridLayout>(R.id.gl_syllable_group)
        var num = "共 ${pinyin?.size.toString()} 个"
        findViewById<TextView>(R.id.syllable_number).text = num

        gridLayout.removeAllViews()
        gridLayout.columnCount = 3  // 3 列

        for ((index, pinyinItem) in list.withIndex()) {
            // 创建 TextView
            val textView = TextView(this).apply {
                text = pinyinItem
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(16, 12, 16, 12)

                setBackgroundResource(R.drawable.input_box)
                setTextColor(Color.BLACK)

                setOnClickListener {
                    // 跳转到其他Activity，并传递拼音
                    val intent = Intent(this@SyllablePractice, SpellPractise::class.java).apply {
                        putStringArrayListExtra("pinyin_list",
                            ArrayList(list))                    // 传递整个拼音列表
                        putExtra("index", index)            // 传递当前索引
                    }
                    startActivity(intent)
                }

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