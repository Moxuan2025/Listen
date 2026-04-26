package com.demo.listen.Layout.EnjoyStudy

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.R
import com.demo.listen.model.ChatMessage
import com.demo.listen.net.HunyuanHelper
import kotlin.concurrent.thread

// 对话场景数据类（包含系统提示词）
data class ChatScene(val name: String, val systemPrompt: String)

class SceneChatActivity : AppCompatActivity() {

    private lateinit var tvSceneTitle: TextView
    private lateinit var btnChangeScene: Button
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button

    private lateinit var adapter: ChatMessageAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private var currentScene: ChatScene? = null

    private val scenes = listOf(
        ChatScene("餐厅点餐", "你是一位餐厅服务员，帮客人点餐，推荐特色菜。"),
        ChatScene("机场问路", "你是机场地勤人员，为旅客指路，解答航班问题。"),
        ChatScene("酒店入住", "你是酒店前台，为客人办理入住，说明设施。"),
        ChatScene("购物咨询", "你是一位商店导购员，帮助顾客挑选商品。"),
        ChatScene("医院问诊", "你是一位医生，耐心询问病人症状并给出建议。")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scene_chat)

        bindViews()
        initRecyclerView()

        // 接收传递的场景名称
        val targetSceneName = intent.getStringExtra("scene_name") ?: "通用对话"
        val systemPrompt = generateSystemPrompt(targetSceneName)
        
        // 初始化当前场景
        currentScene = ChatScene(targetSceneName, systemPrompt)
        tvSceneTitle.text = targetSceneName

        // 清空聊天列表并添加欢迎语
        chatMessages.clear()
        val welcomeMsg = "你好！我是${targetSceneName}场景中的助手，请问有什么可以帮您？"
        chatMessages.add(ChatMessage(content = welcomeMsg, isUser = false))
        adapter.updateMessages(chatMessages.toList())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bindViews() {
        tvSceneTitle = findViewById(R.id.tv_scene_title)
        btnChangeScene = findViewById(R.id.btn_change_scene)
        rvChatMessages = findViewById(R.id.rv_chat_messages)
        etInput = findViewById(R.id.et_input)
        btnSend = findViewById(R.id.btn_send)
    }

    private fun initRecyclerView() {
        adapter = ChatMessageAdapter(chatMessages)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter

        btnSend.setOnClickListener {
            sendMessage()
        }

        btnChangeScene.setOnClickListener {
            showSceneSelector()
        }
    }

    private fun showSceneSelector() {
        val sceneNames = scenes.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("切换对话场景")
            .setItems(sceneNames) { _, which ->
                val selected = scenes[which]
                currentScene = selected
                tvSceneTitle.text = selected.name
                chatMessages.clear()
                chatMessages.add(ChatMessage(content = "已切换到${selected.name}场景。${selected.systemPrompt}", isUser = false))
                adapter.updateMessages(chatMessages.toList())
                scrollToBottom()
            }
            .show()
    }

    private fun generateSystemPrompt(sceneName: String): String {
        return when (sceneName) {
            "购物" -> "你是一位商店导购员，热情帮助顾客挑选商品，介绍产品特点。"
            "日常游玩" -> "你是一位旅游向导，为游客推荐景点，介绍当地文化。"
            "生活服务" -> "你是一位社区服务人员，解答居民关于生活设施的问题。"
            "情绪表达" -> "你是一位心理咨询师，耐心倾听用户的烦恼，给予情感支持。"
            "居家" -> "你是一位家庭助手，提供家务建议和日常生活小贴士。"
            "兴趣爱好" -> "你是一位兴趣导师，与用户交流爱好，分享相关知识和技巧。"
            "问路" -> "你是一位路人或地勤人员，对方向你问路。请用中文清晰指路，态度友好。"
            "借书" -> "你是一位图书馆管理员，对方想借书。请用中文询问需求，告知借书规则。"
            "课堂交流" -> "你是一位老师或同学，在课堂上进行交流讨论。"
            "校园活动" -> "你是一位学生会成员，正在组织或介绍校园活动。"
            "同学互动" -> "你是一位同班同学，正在和对方闲聊学校生活。"
            "体育运动" -> "你是一位体育教练或球友，正在交流运动技巧和心得。"
            else -> "请扮演 $sceneName 场景中的角色，用中文与用户进行自然、友好的多轮对话。"
        }
    }

    private fun sendMessage() {
        val input = etInput.text.toString().trim()
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentScene == null) {
            Toast.makeText(this, "请先选择场景", Toast.LENGTH_SHORT).show()
            return
        }

        // 添加用户消息
        val userMessage = ChatMessage(content = input, isUser = true)
        chatMessages.add(userMessage)
        adapter.updateMessages(chatMessages.toList())
        scrollToBottom()

        // 清空输入框
        etInput.setText("")

        // 禁用发送按钮，防止重复提交
        btnSend.isEnabled = false

        // 构造提示词
        val prompt = buildPrompt(currentScene!!.systemPrompt, chatMessages)

        // 调用混元 API
        thread {
            val result = HunyuanHelper.chat(prompt, this@SceneChatActivity)
            runOnUiThread {
                btnSend.isEnabled = true
                if (result.startsWith("异常") || result.startsWith("密钥") || result.startsWith("API错误")) {
                    Toast.makeText(this, "AI回复失败: $result", Toast.LENGTH_LONG).show()
                } else {
                    val aiMessage = ChatMessage(content = result, isUser = false)
                    chatMessages.add(aiMessage)
                    adapter.updateMessages(chatMessages.toList())
                    scrollToBottom()
                }
            }
        }
    }

    private fun buildPrompt(systemPrompt: String, messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        sb.append("系统：$systemPrompt\n\n")
        
        // 限制保留最近 10 轮对话（20条消息），避免 token 超限
        val recentMessages = if (messages.size > 20) messages.takeLast(20) else messages
        
        for (msg in recentMessages) {
            if (msg.isUser) {
                sb.append("用户：${msg.content}\n")
            } else {
                sb.append("助手：${msg.content}\n")
            }
        }
        
        // 最后一条应该是用户当前输入，等待 AI 回复
        sb.append("助手：")
        return sb.toString()
    }

    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
        }
    }
}
