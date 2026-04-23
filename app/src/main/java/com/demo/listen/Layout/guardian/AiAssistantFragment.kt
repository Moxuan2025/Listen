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

        lifecycleScope.launch {
            try {
                // TODO: 调用 AI 接口（预留）
                // val answer = ServerApi.askAi(question)
                val fakeAnswer = "这是关于「$question」的模拟回答。\n\n实际使用时请替换为真实 AI 接口返回的健康建议。"
                appendMessage("🤖 助手：$fakeAnswer")
            } catch (e: Exception) {
                appendMessage("❌ 请求失败：${e.message}")
                Toast.makeText(requireContext(), "AI 服务暂时不可用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun appendMessage(text: String) {
        tvChatHistory.append("$text\n\n")
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }
}