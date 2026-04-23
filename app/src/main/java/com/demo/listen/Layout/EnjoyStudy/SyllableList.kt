package com.demo.listen.Layout.EnjoyStudy

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.Layout.EnjoyStudy.SyllablePractice
import com.demo.listen.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SyllableList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_syllable_list)

        addSyllable()
        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun addSyllable() {
        val pinyinMap = getPinyinData(this@SyllableList)
        // 声母列表（21个）
        val initials = listOf(
            "b", "p", "m", "f", "d", "t", "n", "l",
            "g", "k", "h", "j", "q", "x",
            "zh", "ch", "sh", "r", "z", "c", "s"
        )

        val gridLayout = findViewById<GridLayout>(R.id.gl_list)
        gridLayout.columnCount = 3

        for (initial in initials) {
            val textView = TextView(this).apply {
                text = initial
                gravity = Gravity.CENTER
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.bg_syllable_item)

                // 点击事件
                setOnClickListener {
                    val intent = Intent(this@SyllableList,
                        SyllablePractice::class.java)
                    // 传入声母字符
                    val pyList = pinyinMap[initial]
                    intent.putStringArrayListExtra("Pinyin",
                        ArrayList(pyList ?: emptyList()))
                    intent.putExtra("Syllable", initial)
                    startActivity(intent)
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(8, 8, 8, 8)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }

            textView.layoutParams = params
            gridLayout.addView(textView)
        }
    }

    private fun handleClick() {
        findViewById<ImageButton>(R.id.bt_sl_back).setOnClickListener {
            finish()
        }
    }

    // 解析 JSON，获取拼音
    fun getPinyinData(context: Context): Map<String, List<String>> {
        return try {
            // 修正文件名为 PinYin.json（注意大小写）
            val jsonString = context.assets.open("PinYin.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            Gson().fromJson(jsonString, type) ?: emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            // 打印详细错误信息
            println("读取文件错误: ${e.message}")
            emptyMap()
        }
    }
}