package com.demo.listen.Layout.Assessment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R

class DisabilityLevel : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_disability_level)

        // [核心修复] 接收并保存 child_username，以便传递给下一级
        val childUsername = intent.getStringExtra("child_username") ?: ""
        android.util.Log.e("DISABILITY_LEVEL", "Received child_username: $childUsername")

        initDisabilityList(childUsername)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initDisabilityList(childUsername: String) {
        val container = findViewById<LinearLayout>(R.id.disability_level_list)

        // 先清空原有内容（如果之前有测试数据）
        container.removeAllViews()

        // 添加“正常”选项，level = -1
        container.addView(createLevelItem("正常", -1, childUsername))

        val levels = listOf("一级(极重度)", "二级(重度)", "三级(中度)", "四级(轻度)")
        levels.forEachIndexed { index, level ->
            container.addView(createLevelItem(level, index + 1, childUsername))
        }
    }

    private fun createLevelItem(text: String, levelValue: Int, childUsername: String): TextView {
        return TextView(this).apply {
            this.text = text
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
            setBackgroundResource(R.drawable.green_bg)

            setOnClickListener {
                startActivity(Intent(this@DisabilityLevel,
                    AssessmentActivity::class.java).apply {
                    putExtra("level", levelValue)
                    putExtra("child_username", childUsername) // [核心修复] 传递孩子用户名
                })
            }
        }
    }
}