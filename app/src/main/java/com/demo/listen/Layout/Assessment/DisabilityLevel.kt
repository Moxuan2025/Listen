package com.demo.listen.Layout.Assessment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import com.demo.listen.R

class DisabilityLevel : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_disability_level)

        initDisabilityList()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initDisabilityList() {
        val levels = listOf("一级(极重度)", "二级(重度)", "三级(中度)", "四级(轻度)")
        val container = findViewById<LinearLayout>(R.id.disability_level_list)

        levels.forEachIndexed { index, level ->
            val textView = TextView(this).apply {
                text = level
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        60f,
                        resources.displayMetrics
                    ).toInt()
                )
                params.topMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20f,
                    resources.displayMetrics
                ).toInt()
                layoutParams = params

                setPadding(0, 0, 0, 0)
                setBackgroundResource(R.drawable.green_bg)

                // 设置点击事件
                setOnClickListener {
                    handleLevelClick(index)
                }
            }
            container.addView(textView)
        }
    }

    private fun handleLevelClick(level: Int) {
        startActivity(Intent(this@DisabilityLevel,
            Assess::class.java).apply {
                putExtra("level", level)
        })
    }
}