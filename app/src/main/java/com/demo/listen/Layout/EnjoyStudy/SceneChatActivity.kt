package com.demo.listen.Layout.EnjoyStudy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.R
import com.demo.listen.model.ChatMessage
import com.demo.listen.net.AsrHelper
import com.demo.listen.net.HunyuanHelper
import com.demo.listen.net.TencentSpeechHelper
import kotlin.concurrent.thread

// 对话场景数据类（包含系统提示词）
data class ChatScene(val name: String, val systemPrompt: String)

class SceneChatActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SceneChatActivity"
    }

    private lateinit var tvSceneTitle: TextView
    private lateinit var btnChangeScene: Button
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private lateinit var tvStatus: TextView
    private lateinit var btnRecord: Button

    private lateinit var adapter: ChatMessageAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private var currentScene: ChatScene? = null
    private var expectedAnswers = listOf<String>()
    private val sessionScores = mutableListOf<Float>()
    private var isListening = false
    private var roundCount = 0
    private val MAX_ROUNDS = 50

    private val scenes = listOf(
        ChatScene("餐厅点餐", "你是一位餐厅服务员，帮客人点餐，推荐特色菜。"),
        ChatScene("机场问路", "你是机场地勤人员，为旅客指路，解答航班问题。"),
        ChatScene("酒店入住", "你是酒店前台，为客人办理入住，说明设施。"),
        ChatScene("购物咨询", "你是一位商店导购员，帮助顾客挑选商品。"),
        ChatScene("医院问诊", "你是一位医生，耐心询问病人症状并给出建议。")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_chat)

        bindViews()
        initRecyclerView()

        // 接收传递的场景名称
        val targetSceneName = intent.getStringExtra("scene_name") ?: "通用对话"
        val systemPrompt = generateSystemPrompt(targetSceneName) + 
            "\n注意：请在回复的最后用 [候选回答1|候选回答2|候选回答3] 的格式提供三个小朋友可能说的简短回答，如果实在想不出候选回答，也可以不写。"

        // 初始化当前场景
        currentScene = ChatScene(targetSceneName, systemPrompt)
        tvSceneTitle.text = targetSceneName

        // 清空聊天列表并添加欢迎语
        chatMessages.clear()
        val welcomeMsg = "你好！我是${targetSceneName}场景中的助手，请问有什么可以帮您？"
        chatMessages.add(ChatMessage(content = welcomeMsg, isUser = false))
        adapter.updateMessages(chatMessages.toList())
        scrollToBottom()

        // 启动全语音交互流程
        startVoiceRound(welcomeMsg)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleRecording()
        } else {
            updateStatus("需要录音权限才能进行语音交互")
            Toast.makeText(this, "需要录音权限才能使用语音功能", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleRecording() {
        if (isListening) {
            AsrHelper.stopRecognize()
            updateStatus("正在处理...")
            btnRecord.text = "开始说话"
            btnRecord.isEnabled = false
        } else {
            startListening()
        }
    }

    private fun bindViews() {
        tvSceneTitle = findViewById(R.id.tv_scene_title)
        btnChangeScene = findViewById(R.id.btn_change_scene)
        rvChatMessages = findViewById(R.id.rv_chat_messages)
        etInput = findViewById(R.id.et_input)
        btnSend = findViewById(R.id.btn_send)
        tvStatus = findViewById(R.id.tv_status)
        btnRecord = findViewById(R.id.btn_record)

        // 设置录音按钮点击监听
        btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                toggleRecording()
            }
        }
    }

    private fun initRecyclerView() {
        adapter = ChatMessageAdapter(chatMessages)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter

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
        val coreRules = """
            【核心对话规则】
            1. 每次说1-2句话，尽量简短。如果需要说第三句来安慰或引导，可以多说一句，但不要长篇大论。
            2. 你是一个引导者，用提问或鼓励的方式和小朋友聊天，鼓励ta多说。
            3. 用词要简单，像对幼儿园大班的小朋友说话。
            4. 当小朋友说得好时，先肯定（“说得真好！”“好棒！”），再自然接话。
            5. 不要一次性介绍完所有内容，要通过提问一点点引出。
            6. 如果小朋友说了不礼貌、伤害别人或涉及危险的话，温和地告诉ta“这样说可能不太好哦”，并示范一句正确的说法。
            7. 如果小朋友跑题了，温柔地带回来：“我们先把这个练完好不好？”然后问一个与场景相关的问题。
        """.trimIndent()

        return when (sceneName) {
            "购物" -> """你是一位友善的店员，小朋友来买东西。
$coreRules
具体场景要点：
- 先打招呼，问“你想买什么呀？”
- 每次推荐一个东西，介绍一句，再问“这个你喜欢吗？”
- 最后说再见时要温暖。
"""

            "日常游玩" -> """你是一位亲切的导游，带小朋友游玩。
$coreRules
具体场景要点：
- 先问“你想去哪里玩呀？”
- 一次只介绍一个景点，用一句有趣的话，再问“你看到了什么？”
- 要提醒注意安全，但不要吓到孩子。
"""

            "生活服务" -> """你是一位热心的社区阿姨，小朋友来求助。
$coreRules
具体场景要点：
- 先安慰“别着急，慢慢说”，再问“需要阿姨帮你做什么？”
- 每次给出一个小建议，然后问“你觉得这样行吗？”
- 结束时表扬小朋友自己能解决问题。
"""

            "情绪表达" -> """你是一位温柔的心理老师，小朋友心情不好。
$coreRules
具体场景要点：
- 先引导“来，告诉老师怎么啦？”，等待回答。
- 每次回应先说出孩子的感受（“老师知道你有点难过”），再轻轻问一个问题。
- 不要一次性给很多建议，每次只说一个。
"""

            "居家" -> """你是一位耐心的家庭老师，教小朋友做家务。
$coreRules
具体场景要点：
- 一次只教一个动作，用比喻（像卷蛋糕一样），然后问“你试试看？”
- 孩子尝试后立刻夸奖，再教下一步。
- 绝不批评，只说“再练习一次就好啦”。
"""

            "兴趣爱好" -> """你是一位友好的大哥哥/大姐姐，和小朋友聊爱好。
$coreRules
具体场景要点：
- 先问“你最喜欢做什么呀？”，听孩子说完再分享你自己的小故事。
- 每次分享要简短，然后继续问“你呢？你还喜欢什么？”
- 要让对话像朋友聊天，有来有回。
"""

            "问路" -> """你是一位和蔼的路人，小朋友向你问路。
$coreRules
具体场景要点：
- 指路只说两步，比如“先直走，看到红色大门右转”，然后问“记住了吗？”
- 用标志物，不说抽象距离。
- 一定提醒过马路安全，但语气要温和。
"""

            "借书" -> """你是一位温柔的图书管理员，小朋友来借书。
$coreRules
具体场景要点：
- 先问“你想借什么书呀？”，等孩子回答。
- 每次只推荐一本书，说一句推荐理由，再问“这本你喜欢吗？”
- 规则要简单说一句，比如“可以借7天，要轻轻翻哦”。
"""

            "课堂交流" -> """你是一位亲切的老师，正在和小朋友对话。
$coreRules
具体场景要点：
- 提一个有趣的问题，然后等待，说“不着急，慢慢想”。
- 孩子回答后先表扬，再追问一个相关的小问题。
- 不要说教，要像聊天一样自然引导。
"""

            "校园活动" -> """你是一位活泼的学长/学姐，介绍校园活动。
$coreRules
具体场景要点：
- 先兴奋地说“我们学校有好多好玩的活动！”，然后问“你喜欢画画还是唱歌？”
- 根据孩子回答，一次只介绍一个相关活动，用“超好玩！”的语气，再问“你想试试吗？”
"""

            "同学互动" -> """你是小朋友的新同学，想和ta交朋友。
$coreRules
具体场景要点：
- 先自我介绍：“你好，我叫小乐，我们可以做朋友吗？”等待回应。
- 每次回复都要回应对方的话，再问一个新问题，比如“你下课喜欢玩什么？”
- 要让对话轻松自然，像两个小朋友课间聊天。
"""

            "体育运动" -> """你是一位阳光的体育教练，带小朋友运动。
$coreRules
具体场景要点：
- 一次教一个动作，用鼓励的语气，比如“来，像小青蛙一样跳一下！”
- 孩子做完后立刻喊“好棒！再来一次！”
- 如果孩子说累了，要说“休息一下，喝口水”。
"""

            "餐厅点餐" -> """你是一位热情的服务员，小朋友来餐厅吃饭。
$coreRules
具体场景要点：
- 先问“小朋友，你今天想吃什么好吃的呀？”
- 每次推荐一道菜，描述得香香的，再问“要不要尝尝这个？”
- 记得问“有没有不喜欢吃的东西呀？”
"""

            "机场问路" -> """你是一位机场地勤人员，帮助小朋友找登机口。
$coreRules
具体场景要点：
- 先问“你要去哪个城市呀？把机票给我看看。”
- 指路时用简单的方向，比如“跟着那个小飞机标志走”，然后问“看到了吗？”
- 提醒“抓紧爸爸妈妈的手哦”。
"""

            "酒店入住" -> """你是一位酒店前台，帮小朋友办理入住。
$coreRules
具体场景要点：
- 先笑着说“欢迎来到我们的酒店！”，问“你们订的是哪个房间呀？”
- 介绍设施时说“那边有游泳池哦”，再问“你想去看看吗？”
- 说话要慢一点，确保小朋友听懂了。
"""

            else -> """你是一位友好的对话伙伴，和小朋友聊天。
$coreRules
具体场景要点：
- 先友好地问好，问一个简单的问题。
- 每次回复要简短，留出空间给小朋友说话。
- 始终保持鼓励和温暖。
"""
        }
    }

    private fun startVoiceRound(aiText: String, isLastRound: Boolean = false) {
        updateStatus("AI 正在说话...")
        btnRecord.isEnabled = false // 播放时禁用录音按钮
        btnRecord.text = "开始说话"

        // 解析预设答案
        val regex = Regex("\\[(.*?)\\]")
        val match = regex.find(aiText)
        if (match != null) {
            expectedAnswers = match.groupValues[1].split("|").map { it.trim() }
        } else {
            // 默认三个简单回答
            expectedAnswers = listOf("是的", "好的", "谢谢")
        }

        // 只播放 TTS，不再自动启动录音
        TencentSpeechHelper.synthesisAndPlay(aiText, this) {
            runOnUiThread {
                if (isLastRound) {
                    finishSession()
                } else {
                    updateStatus("请点击“开始说话”进行回复")
                    btnRecord.isEnabled = true
                }
            }
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        try {
            updateStatus("正在聆听...")
            btnRecord.text = "停止说话"
            isListening = true
            AsrHelper.startRecognize(
                context = this,
                engineModelType = "16k_zh",
                onComplete = { text ->
                    if (!isFinishing && !isDestroyed) {
                        runOnUiThread { handleUserInput(text) }
                    }
                },
                onError = { code, msg ->
                    if (!isFinishing && !isDestroyed) {
                        runOnUiThread {
                            updateStatus("识别错误: $msg")
                            isListening = false
                            btnRecord.text = "开始说话"
                            btnRecord.isEnabled = true
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "启动录音异常", e)
            updateStatus("录音启动失败")
            isListening = false
            btnRecord.text = "开始说话"
            btnRecord.isEnabled = true
        }
    }

    private fun handleUserInput(userText: String) {
        isListening = false
        if (isFinishing) return
        btnRecord.text = "开始说话"
        updateStatus("识别结果：$userText")

        // 新增：结束意图识别
        val endKeywords = listOf(
            "再见", "拜拜", "结束", "不说了", "不想说", "停止", "算了", "我要走了",
            "不想玩了", "不玩了", "没意思", "无聊", "累了", "不聊了", "不想回答",
            "别问了", "烦死了", "走开", "不要了", "不做了", "就这样吧"
        )
        val hasNegation = userText.length <= 3 && (userText.contains("不") || userText.contains("没") || userText.contains("别"))
        
        if (endKeywords.any { userText.contains(it) } || hasNegation) {
            processEnding(userText)
            return
        }

        // 添加用户消息到界面
        chatMessages.add(ChatMessage(content = userText, isUser = true))
        adapter.updateMessages(chatMessages.toList())
        scrollToBottom()

        // 计算匹配度
        if (expectedAnswers.isNotEmpty()) {
            var maxScore = 0f
            var count = 0
            for (answer in expectedAnswers) {
                try {
                    HunyuanHelper.getSimilarity(userText, answer, this) { similarity ->
                        if (similarity > maxScore) maxScore = similarity
                        count++
                        if (count == expectedAnswers.size) {
                            sessionScores.add(maxScore)
                            processNextTurn(userText)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "相似度计算异常", e)
                    count++
                    if (count == expectedAnswers.size) {
                        processNextTurn(userText)
                    }
                }
            }
        } else {
            processNextTurn(userText)
        }
    }

    private fun processNextTurn(userText: String) {
        val systemPrompt = currentScene?.systemPrompt ?: ""
        updateStatus("AI 思考中...")
    
        // 构建结构化消息列表
        val messages = buildMessageListForChat(systemPrompt)
    
        thread {
            try {
                HunyuanHelper.getChatResponse(
                    messages = messages,
                    systemPrompt = systemPrompt,
                    context = this@SceneChatActivity,
                    onResult = { aiResponse ->
                        if (!isFinishing && !isDestroyed) {
                            runOnUiThread {
                                val (displayText, answers) = parseResponse(aiResponse)
                                val finalText = if (displayText.isBlank()) "嗯，继续说哦" else displayText
                                chatMessages.add(ChatMessage(content = finalText, isUser = false))
                                adapter.updateMessages(chatMessages.toList())
                                scrollToBottom()
                                roundCount++
                                startVoiceRound(finalText, roundCount >= MAX_ROUNDS)
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "AI 请求异常", e)
                if (!isFinishing && !isDestroyed) {
                    runOnUiThread {
                        updateStatus("AI 回复失败，请重试")
                        btnRecord.isEnabled = true
                    }
                }
            }
        }
    }

    private fun parseResponse(raw: String): Pair<String, List<String>> {
        val regex = Regex("\\[(.*?)\\]")
        val match = regex.find(raw)
        val answers = if (match != null) {
            match.groupValues[1].split("|").map { it.trim() }
        } else {
            listOf()
        }
        val cleanText = raw.replace(regex, "").trim()
            .removePrefix("助手：").removePrefix("助手:").trim()
        return Pair(cleanText, answers)
    }

    private fun processEnding(userText: String) {
        // 添加用户消息
        chatMessages.add(ChatMessage(content = userText, isUser = true))
        adapter.updateMessages(chatMessages.toList())
        scrollToBottom()

        val systemPrompt = currentScene?.systemPrompt ?: ""
        // 构建告别消息列表：在现有对话基础上追加一条指令
        val farewellMessages = buildMessageListForChat(systemPrompt).toMutableList()
        farewellMessages.add(mapOf("role" to "user", "content" to "用户说：$userText\n请用一句温暖简短的话告别小朋友，并祝他开心。"))

        thread {
            try {
                HunyuanHelper.getChatResponse(
                    messages = farewellMessages,
                    systemPrompt = systemPrompt,
                    context = this@SceneChatActivity,
                    onResult = { farewell ->
                        if (!isFinishing && !isDestroyed) {
                            runOnUiThread {
                                val cleanFarewell = farewell.replace(Regex("\\[.*?\\]"), "").trim()
                                    .removePrefix("助手：").removePrefix("助手:").trim()
                                val finalFarewell = if (cleanFarewell.isBlank()) "再见啦，今天聊得很开心哦！" else cleanFarewell
                                chatMessages.add(ChatMessage(content = finalFarewell, isUser = false))
                                adapter.updateMessages(chatMessages.toList())
                                scrollToBottom()
                                // 播放告别语，结束后再弹出分数
                                startVoiceRound(finalFarewell, isLastRound = true)
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "告别语生成异常", e)
                runOnUiThread { finishSession() }
            }
        }
    }

    private fun finishSession() {
        val averageScore = if (sessionScores.isNotEmpty()) sessionScores.average() else 0.0
        AlertDialog.Builder(this)
            .setTitle("练习完成")
            .setMessage("您的平均匹配度成绩为：${String.format("%.2f", averageScore * 100)} 分")
            .setPositiveButton("确定") { dialog, _ ->
                AsrHelper.cancelRecognize()
                TencentSpeechHelper.release()
                finish()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateStatus(status: String) {
        tvStatus.text = status
    }

    private fun buildMessageListForChat(systemPrompt: String): List<Map<String, String>> {
        val messages = mutableListOf<Map<String, String>>()
        // 限制最近 20 条消息
        val recent = if (chatMessages.size > 20) chatMessages.takeLast(20) else chatMessages
        for (msg in recent) {
            messages.add(mapOf(
                "role" to if (msg.isUser) "user" else "assistant",
                "content" to msg.content
            ))
        }
        return messages
    }
    
    /* 旧的方法已弃用，改用结构化消息列表
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
    */

    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
        }
    }
}
