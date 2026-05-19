package com.demo.listen.Layout.Assessment

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R
import com.demo.listen.net.ServerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                // 显示 loading 提示
                val progressDialog = ProgressDialog(this@DisabilityLevel).apply {
                    setMessage("正在保存听力等级...")
                    setCancelable(false)
                    show()
                }
                
                // 将用户选择的等级映射为档案需要的字符串
                val hearingLevelStr = when (levelValue) {
                    1 -> "极重度"
                    2 -> "重度"
                    3 -> "中度"
                    4 -> "轻度"
                    else -> "无听力损失"
                }
                
                // 异步调用更新 API
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val success = ServerApi.updateChildHearingLevel(childUsername, hearingLevelStr)
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            if (success) {
                                Toast.makeText(this@DisabilityLevel, "听力等级已保存", Toast.LENGTH_SHORT).show()
                            } else {
                                android.util.Log.e("DISABILITY_LEVEL", "更新听力等级失败")
                                Toast.makeText(this@DisabilityLevel, "保存失败，仍可继续评估", Toast.LENGTH_SHORT).show()
                            }
                            // 无论更新是否成功，都继续跳转到评估页面
                            startActivity(Intent(this@DisabilityLevel,
                                AssessmentActivity::class.java).apply {
                                putExtra("level", levelValue)
                                putExtra("child_username", childUsername)
                            })
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DISABILITY_LEVEL", "更新异常", e)
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            Toast.makeText(this@DisabilityLevel, "网络错误，继续评估", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@DisabilityLevel,
                                AssessmentActivity::class.java).apply {
                                putExtra("level", levelValue)
                                putExtra("child_username", childUsername)
                            })
                        }
                    }
                }
            }
        }
    }
}