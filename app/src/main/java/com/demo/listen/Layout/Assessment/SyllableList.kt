package com.demo.listen.Layout.Assessment

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

class SyllableList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_syllable_list)

        addSyllable()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun addSyllable() {
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
                    val intent = Intent(this@SyllableList, SyllablePractice::class.java)
                    // 传入声母字符
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
}