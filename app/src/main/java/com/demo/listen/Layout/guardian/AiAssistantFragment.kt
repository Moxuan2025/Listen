package com.demo.listen.Layout.guardian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.demo.listen.R
import com.demo.listen.net.HunyuanHelper
import kotlinx.coroutines.launch

class AiAssistantFragment : Fragment() {

    private lateinit var tvChatHistory: TextView
    private lateinit var etQuestion: EditText
    private lateinit var btnSend: Button
    private lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_assistant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvChatHistory = view.findViewById(R.id.tv_chat_history)
        etQuestion = view.findViewById(R.id.et_question)
        btnSend = view.findViewById(R.id.btn_send)
        scrollView = view.findViewById(R.id.scroll_view)

        btnSend.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                sendQuestion(question)
                etQuestion.text.clear()
            }
        }

        // 添加欢迎语
        appendMessage("👩‍⚕️ 你好！我是健康知识助手，有什么可以帮你的？")
    }

    private fun sendQuestion(question: String) {
        appendMessage("👤 你：$question")
        btnSend.isEnabled = false

        lifecycleScope.launch {
            try {
                // 构建符合“儿童学习成长助手”身份的系统提示词
                val systemPrompt = """
                    你是一位服务于家长的**儿童学习成长助手**，具备亲切、耐心、专业的语气。

                    【核心能力与边界】
                    1. 你可以查询孩子的学习档案、评估报告（如听力、表达、理解维度）。
                    2. 基于档案数据提供学习建议，回答教育方法、亲子沟通等日常咨询。
                    3. **严禁**提供医疗诊断、心理治疗建议或任何超出教育辅导范畴的结论。
                    4. 当需要展示具体数据时，必须引用从档案中查到的真实值，不得捏造。
                    5. 语气要温暖，多使用鼓励性语言，强调进步而非缺陷。

                    【当前孩子档案参考】
                    - 姓名：小明
                    - 年龄：6岁
                    - 最近评估：听力 85分，表达 78分，理解 92分，综合 85分。

                    【家长问题】
                    $question

                    请给出简洁、温暖且专业的建议：
                """.trimIndent()

                // 调用混元 AI
                val answer = HunyuanHelper.chat(systemPrompt, requireContext())
                appendMessage("🤖 助手：$answer")
            } catch (e: Exception) {
                appendMessage("❌ 请求失败：${e.message}")
            } finally {
                btnSend.isEnabled = true
            }
        }
    }

    private fun appendMessage(text: String) {
        tvChatHistory.append("$text\n\n")
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }
}