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
        btnSend.isEnabled = false // 防止重复点击

        lifecycleScope.launch {
            try {
                // 1. 准备孩子档案信息（这里可以先写死，后续改为从 ServerApi 获取）
                val childProfile = """
                    姓名：小明
                    年龄：6岁
                    最近评估结果：听力能力 85分，表达能力 70分，理解能力 90分。
                    健康状况：无过敏史，近期有轻微咳嗽。
                """.trimIndent()

                // 2. 构建带有角色设定的提示词
                val systemPrompt = """
                    你是一位专业的儿童健康与教育顾问。请根据以下孩子档案回答家长的问题。
                    如果孩子档案中没有相关信息，请给出通用的健康或教育建议。
                    
                    【孩子档案】
                    $childProfile
                    
                    【家长问题】
                    $question
                    
                    请给出简洁、温暖且专业的建议：
                """.trimIndent()

                // 3. 调用混元 AI (使用 getSimilarity 的底层逻辑，但这里我们需要聊天功能)
                // 注意：之前的 HunyuanHelper.getSimilarity 是专门用于计算相似度的。
                // 为了通用性，我们直接调用 HunyuanHelper 的聊天接口（如果存在）或者复用其签名逻辑发起 ChatCompletions 请求。
                // 鉴于 HunyuanHelper 目前只暴露了 getSimilarity，我们暂时复用其内部逻辑发起一次标准的聊天请求。
                
                // 为了简化，我们假设 HunyuanHelper 增加了一个 chat 方法，或者我们在这里直接构造请求。
                // 由于不能修改 HunyuanHelper 太多，我们暂时使用一个简化的方式：
                // 实际上，getSimilarity 内部就是调用 ChatCompletions。我们可以微调一下 prompt 让它直接返回建议。
                
                // 这里我们直接使用 HunyuanHelper 现有的能力，但为了效果更好，建议在 HunyuanHelper 中增加一个通用的 chat 方法。
                // 临时方案：我们直接调用 HunyuanHelper 的一个新扩展方法（稍后在 HunyuanHelper 中添加）
                
                val answer = HunyuanHelper.chat(systemPrompt, requireContext())
                appendMessage("🤖 助手：$answer")
            } catch (e: Exception) {
                appendMessage("❌ 请求失败：${e.message}")
                Toast.makeText(requireContext(), "AI 服务暂时不可用", Toast.LENGTH_SHORT).show()
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