package com.demo.listen.Layout.guardian

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.demo.listen.R
import com.demo.listen.net.ServerApi
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChildDetailActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvHearingLevel: TextView
    private lateinit var tvListeningScore: TextView
    private lateinit var tvExpressionScore: TextView
    private lateinit var tvComprehensionScore: TextView
    private lateinit var tvOverallScore: TextView

    private var childUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_detail)

        childUsername = intent.getStringExtra("child_username") ?: ""
        if (childUsername.isEmpty()) {
            Toast.makeText(this, "无效的孩子信息", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        loadProfile()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bindViews() {
        tvName = findViewById(R.id.tv_child_name)
        tvHearingLevel = findViewById(R.id.tv_hearing_level)
        tvListeningScore = findViewById(R.id.tv_listening_score)
        tvExpressionScore = findViewById(R.id.tv_expression_score)
        tvComprehensionScore = findViewById(R.id.tv_comprehension_score)
        tvOverallScore = findViewById(R.id.tv_overall_score)
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val profile = ServerApi.getChildProfile(childUsername)
                updateUI(profile)
            } catch (e: Exception) {
                Toast.makeText(this@ChildDetailActivity, "加载档案失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(profile: JSONObject) {
        tvName.text = profile.optString("name", childUsername)
        tvHearingLevel.text = "听力等级: ${profile.optString("hearing_loss_level", "未知")}"
        
        // 简单展示最近一次分数，如果没有则显示“暂无数据”
        val listeningScores = profile.optJSONArray("listening_scores")
        tvListeningScore.text = "听力均分: ${if (listeningScores != null && listeningScores.length() > 0) listeningScores.getDouble(listeningScores.length() - 1) else "暂无"}"

        val expressionScores = profile.optJSONArray("expression_scores")
        tvExpressionScore.text = "表达均分: ${if (expressionScores != null && expressionScores.length() > 0) expressionScores.getDouble(expressionScores.length() - 1) else "暂无"}"

        val comprehensionScores = profile.optJSONArray("comprehension_scores")
        tvComprehensionScore.text = "理解均分: ${if (comprehensionScores != null && comprehensionScores.length() > 0) comprehensionScores.getDouble(comprehensionScores.length() - 1) else "暂无"}"

        val overallScores = profile.optJSONArray("overall_scores")
        tvOverallScore.text = "综合均分: ${if (overallScores != null && overallScores.length() > 0) overallScores.getDouble(overallScores.length() - 1) else "暂无"}"
    }
}
