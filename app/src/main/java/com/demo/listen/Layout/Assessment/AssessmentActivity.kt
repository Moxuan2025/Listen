package com.demo.listen.Layout.Assessment

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.demo.listen.R
import com.demo.listen.net.OralEvaluationHelper
import com.demo.listen.net.TencentSpeechHelper
//import com.demo.listen.net.NLPHelper
import com.demo.listen.net.ServerApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.demo.listen.net.HunyuanHelper
import com.tencent.cloud.soe.TAIOralController
import android.graphics.Color
class AssessmentActivity : AppCompatActivity() {

    private lateinit var childUsername: String
    private var sessionId: String? = null
    private val answers = mutableMapOf<Int, String>()
    private var assessmentLevel = -1  // 默认正常

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        // [核心修复] 接收孩子用户名，如果为空则打印严重警告
        childUsername = intent.getStringExtra("child_username") ?: ""
        Log.e("ASSESS_INIT", "Received child_username from Intent: '$childUsername'")
        
        if (childUsername.isEmpty()) {
            Log.e("ASSESS_INIT", "CRITICAL: child_username is EMPTY! Please check the calling Activity.")
            // 临时调试：如果为空，可以尝试硬编码测试后端（正式环境请注释掉）
            // childUsername = "moxuan" 
        }

        val level = intent.getIntExtra("level", 1)
        assessmentLevel = level
        if (savedInstanceState == null) {
            onLevelSelected(level)
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.assessment_container, fragment)
            .commit()
    }

    fun onLevelSelected(level: Int) {
        // [调试] 确认 childUsername 是否有效
        android.util.Log.e("ASSESS", "Starting assessment for child: '$childUsername'")
        if (childUsername.isEmpty()) {
            android.util.Log.e("ASSESS", "ERROR: childUsername is empty! Profile will not be updated.")
        }

        // 启动一个真实的服务器会话
        GlobalScope.launch {
            try {
                sessionId = ServerApi.startAssessment(childUsername, level)
                android.util.Log.d("ASSESS", "Session started: $sessionId")
            } catch (e: Exception) {
                sessionId = "fallback-${System.currentTimeMillis()}"
                android.util.Log.e("ASSESS", "Failed to start session: ${e.message}")
            }
            val questions = generateMockQuestions()
            loadFragment(QuestionFragment.newInstance(questions, 0))
        }
    }

    fun saveAnswer(questionIndex: Int, answer: String) {
        answers[questionIndex] = answer
    }

    fun onAllQuestionsFinished() {
        // 1. 听力等级分数
        val levelScore = when (assessmentLevel) {
            -1 -> 100.0
            1 -> 25.0
            2 -> 50.0
            3 -> 75.0
            4 -> 90.0
            else -> 50.0
        }

        // 2. 听力能力分数 (取所有跟读题的平均分，索引5-9)
        val readScores = answers.filterKeys { it in 5..9 }
            .mapNotNull { (_, value) ->
                value.substringAfter("跟读得分：").toDoubleOrNull()
            }
        val avgReadScore = if (readScores.isNotEmpty()) readScores.average() else 0.0

        // 3. 表达能力分数 (暂未实现真实评分，默认75)
        val vocabularyScores = answers.filterKeys { it in 0..4 }
    .mapNotNull { (_, value) ->
        value.substringAfter("词汇得分：").toDoubleOrNull()
    }
val expressionScore = if (vocabularyScores.isNotEmpty()) vocabularyScores.average() else 0.0

        // 4. 阅读能力分数 (从答案中提取NLP评分，索引10-14)
        val readingScores = answers.filterKeys { it in 10..14 }
            .mapNotNull { (_, value) ->
                value.substringAfter("阅读得分：").toDoubleOrNull()
            }
        val avgReadingScore = if (readingScores.isNotEmpty()) readingScores.average() else 0.0

        // 5. 综合分数
        val overall = 0.3 * levelScore + 0.2 * avgReadScore + 0.2 * expressionScore + 0.2 * avgReadingScore

        val result = AssessmentResult(
            listeningScore = levelScore.toFloat(),
            expressionScore = expressionScore.toFloat(),
            comprehensionScore = avgReadingScore.toFloat(),
            overallScore = overall.toFloat()
        )

        // 6. 上传到服务器
        GlobalScope.launch {
            try {
                Log.e("FINISH", "准备上传, sessionId=$sessionId, child=$childUsername")
                ServerApi.finishAssessment(
                    sessionId = sessionId ?: "unknown",
                    level = assessmentLevel,
                    levelScore = levelScore,
                    avgReadScore = avgReadScore,
                    expressionScore = expressionScore,
                    readingScore = avgReadingScore,
                    overallScore = overall,
                    childUsername = childUsername, // [新增] 传递孩子用户名
                    answers = answers
                )
                Log.e("FINISH", "上传成功")
            } catch (e: Exception) {
                Log.e("FINISH", "上传失败", e)
            }
        }

        // 7. 跳转到报告页面
        val intent = Intent(this, com.demo.listen.Layout.ReportPage::class.java).apply {
            putExtra("listening_score", result.listeningScore)
            putExtra("listening_level_score", levelScore.toFloat()) // 传递听力等级分数
            putExtra("expression_score", result.expressionScore)
            putExtra("comprehension_score", result.comprehensionScore)
            putExtra("overall_score", result.overallScore)
        }
        startActivity(intent)
        finish() // 关闭评估活动，防止返回键回到题目页
    }

    private fun generateMockQuestions(): List<AssessmentQuestion> {
        val list = mutableListOf<AssessmentQuestion>()
        
        // 前5题：词语选择（全新多样化题目）
        val wordQuestions = listOf(
            Triple("小猫", listOf("小狗", "小猫", "小鸟", "小鱼"), "小猫"),
            Triple("飞机", listOf("火车", "轮船", "飞机", "汽车"), "飞机"),
            Triple("铅笔", listOf("橡皮", "尺子", "铅笔", "书本"), "铅笔"),
            Triple("红色", listOf("蓝色", "绿色", "黄色", "红色"), "红色"),
            Triple("妈妈", listOf("爸爸", "妈妈", "哥哥", "姐姐"), "妈妈")
        )

        for (i in wordQuestions.indices) {
            val (content, options, answer) = wordQuestions[i]
            list.add(
                AssessmentQuestion(
                    index = i, type = "word_choice",
                    content = "请选出正确的词语：$content",
                    options = options,
                    correctAnswer = answer,
                    audioUrl = null
                )
            )
        }

        // 中5题：跟读
        val sentences = listOf("太阳从东方升起", "我喜欢学习语言", "今天天气真好", "妈妈在厨房做饭", "小明在公园跑步")
        for (i in 5..9) {
            list.add(
                AssessmentQuestion(
                    index = i, type = "read_after",
                    content = "请跟读：${sentences[i - 5]}",
                    options = null,
                    correctAnswer = sentences[i - 5],
                    audioUrl = null
                )
            )
        }

        // 后5题：阅读理解（真实短文）
        val comprehensionData = listOf(
            Triple("小明家有一只白色的小猫，它特别喜欢玩毛线球。每天放学后，小明都会陪小猫玩一会儿。",
                "小明家有什么宠物？",
                "一只白色的小猫"),
            Triple("春天来了，公园里的花都开了，有红的、黄的、粉的。小鸟在树上唱歌，小朋友们在草地上放风筝。",
                "孩子们在草地上做什么？",
                "放风筝"),
            Triple("妈妈今天做了红烧肉和炒青菜，还有一碗热乎乎的鸡蛋汤。小明吃了两碗饭，说妈妈做的饭最好吃。",
                "小明觉得妈妈做的饭怎么样？",
                "最好吃"),
            Triple("学校图书馆里有很多书，有故事书、科普书、还有漫画书。小红最喜欢看故事书，每个星期都要借一本。",
                "小红最喜欢看什么书？",
                "故事书"),
            Triple("昨晚下了一场大雨，今天早上路面还是湿的。爷爷提醒小明出门要带雨伞，因为可能还会下雨。",
                "路面为什么是湿的？",
                "因为昨晚下了大雨")
        )

        for (i in 0..4) {
            val (passage, questionText, answer) = comprehensionData[i]
            list.add(
                AssessmentQuestion(
                    index = 10 + i,
                    type = "comprehension",
                    content = "$passage\n\n$questionText",
                    options = null,
                    correctAnswer = answer,
                    audioUrl = null
                )
            )
        }
        return list
    }
}

// ========== 数据类 ==========
data class AssessmentQuestion(
    val index: Int,
    val type: String,
    val content: String,
    val options: List<String>?,
    val correctAnswer: String,
    val audioUrl: String?
) : java.io.Serializable

data class AssessmentResult(
    val listeningScore: Float,
    val expressionScore: Float,
    val comprehensionScore: Float,
    val overallScore: Float
) : java.io.Serializable

// ========== 选等级 ==========
class SelectLevelFragment : Fragment(R.layout.fragment_select_level) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.level1).setOnClickListener {
            (activity as? AssessmentActivity)?.onLevelSelected(1)
        }
        view.findViewById<Button>(R.id.level2).setOnClickListener {
            (activity as? AssessmentActivity)?.onLevelSelected(2)
        }
        view.findViewById<Button>(R.id.level3).setOnClickListener {
            (activity as? AssessmentActivity)?.onLevelSelected(3)
        }
        view.findViewById<Button>(R.id.level4).setOnClickListener {
            (activity as? AssessmentActivity)?.onLevelSelected(4)
        }
    }
}

// ========== 核心答题Fragment ==========
class QuestionFragment : Fragment(R.layout.fragment_question) {

    companion object {
        private const val ARG_QUESTIONS = "questions"
        private const val ARG_INDEX = "current_index"

        fun newInstance(questions: List<AssessmentQuestion>, currentIndex: Int): QuestionFragment {
            return QuestionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_QUESTIONS, ArrayList(questions))
                    putInt(ARG_INDEX, currentIndex)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecordingAssessment()
        } else {
            Toast.makeText(requireContext(), "需要录音权限才能评测", Toast.LENGTH_SHORT).show()
        }
    }

    private var pendingRefText: String? = null
    private var pendingCallback: ((String) -> Unit)? = null
    private var pendingRecordBtn: Button? = null
    private var pendingReadBtn: Button? = null

    private fun startRecordingAssessment() {
        val refText = pendingRefText ?: return
        val onResult = pendingCallback ?: return
        val recordBtn = pendingRecordBtn ?: return
        val btnRead = pendingReadBtn ?: return

        recordBtn.isEnabled = false
        btnRead.isEnabled = false
        Toast.makeText(requireContext(), "请开始跟读...", Toast.LENGTH_SHORT).show()

        OralEvaluationHelper.startEvaluation(requireContext(), refText) { score ->
            recordBtn.isEnabled = true
            btnRead.isEnabled = true

            if (score.startsWith("评测") || score.startsWith("解析")) {
                Toast.makeText(requireContext(), score, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "得分：$score", Toast.LENGTH_SHORT).show()
            }

            pendingRefText = null
            pendingCallback = null
            pendingRecordBtn = null
            pendingReadBtn = null
            onResult(score)
        }
    }

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val questions = arguments?.getSerializable(ARG_QUESTIONS) as? List<AssessmentQuestion> ?: return
        val currentIndex = arguments?.getInt(ARG_INDEX, 0) ?: 0
        val question = questions[currentIndex]

        val tvProgress = view.findViewById<TextView>(R.id.tvProgress)
        val tvQuestion = view.findViewById<TextView>(R.id.tvQuestion)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.optionsContainer)
        val btnRead = view.findViewById<Button>(R.id.btnRead)
        val comprehensionContainer = view.findViewById<LinearLayout>(R.id.comprehensionContainer)
        val etAnswer = view.findViewById<EditText>(R.id.etAnswer)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        tvProgress.text = "${currentIndex + 1}/${questions.size}"

        if (question.type == "word_choice") {
            tvQuestion.text = "？"
        } else {
            tvQuestion.text = question.content
        }
        tvQuestion.visibility = View.VISIBLE

        optionsContainer.visibility = View.GONE
        btnRead.visibility = View.GONE
        comprehensionContainer.visibility = View.GONE

        when (question.type) {
            "word_choice" -> {
                optionsContainer.visibility = View.VISIBLE
                btnRead.visibility = View.VISIBLE
                btnRead.text = "听题目"

                optionsContainer.removeAllViews()
                val optionButtons = mutableListOf<Button>()
                question.options?.forEach { option ->
                    val btn = Button(optionsContainer.context).apply {
                        text = option
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = 8 }
                    }
                    optionButtons.add(btn)
                    optionsContainer.addView(btn)
                }

                var answered = false
                btnRead.setOnClickListener {
                    btnRead.isEnabled = false
                    TencentSpeechHelper.synthesisAndPlay(question.correctAnswer, requireContext()) {
                        btnRead.isEnabled = true
                    }
                }

                optionButtons.forEach { btn ->
                    btn.setOnClickListener {
                        if (answered) return@setOnClickListener
                        answered = true

                        optionButtons.forEach { it.isEnabled = false }
                        btnRead.isEnabled = false

                        optionButtons.forEach { it.setBackgroundResource(R.drawable.green_bg) }
                        btn.setBackgroundColor(0xFFFF0000.toInt())

                        optionButtons.firstOrNull { it.text == question.correctAnswer }?.let {
                            it.setBackgroundColor(0xFF00AA00.toInt())
                        }

                        val score = if (btn.text == question.correctAnswer) 100 else 0
(activity as? AssessmentActivity)?.saveAnswer(currentIndex, "词汇得分：$score")
                        Handler(Looper.getMainLooper()).postDelayed({
                            goToNext(questions, currentIndex + 1)
                        }, 1000)
                    }
                }
            }

            "read_after" -> {
                btnRead.visibility = View.VISIBLE
                btnRead.text = "听示范音"

                val recordBtn = Button(optionsContainer.context).apply { text = "开始录音" }
                (btnRead.parent as? ViewGroup)?.addView(recordBtn)

                var oralController: TAIOralController? = null
                var isRecording = false

                recordBtn.setOnClickListener {
                    if (isRecording) {
                        // --- 停止录音 ---
                        OralEvaluationHelper.stopManualEvaluation(oralController)
                        recordBtn.text = "🎤 开始录音"
                        btnRead.isEnabled = true
                        isRecording = false
                    } else {
                        // --- 开始录音 ---
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@setOnClickListener
                        }

                        recordBtn.isEnabled = false
                        btnRead.isEnabled = false
                        Toast.makeText(requireContext(), "请跟读...", Toast.LENGTH_SHORT).show()

                        oralController = OralEvaluationHelper.startManualEvaluation(
                            requireContext(),
                            question.correctAnswer
                        ) { score ->
                            // 评测结束（手动停止或异常结束都会回调这里）
                            recordBtn.isEnabled = true
                            btnRead.isEnabled = true
                            recordBtn.text = "🎤 开始录音"
                            isRecording = false

                            if (score.startsWith("评测") || score.startsWith("解析")) {
                                Toast.makeText(requireContext(), score, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "得分：$score", Toast.LENGTH_SHORT).show()
                            }

                            (activity as? AssessmentActivity)?.saveAnswer(currentIndex, "跟读得分：$score")
                            Handler(Looper.getMainLooper()).postDelayed({
                                goToNext(questions, currentIndex + 1)
                            }, 1000)
                        }

                        recordBtn.text = "⏹ 停止录音"
                        isRecording = true
                        recordBtn.isEnabled = true
                    }
                }

                // 听示范音：只播放，不跳题
                btnRead.setOnClickListener {
                    btnRead.isEnabled = false
                    TencentSpeechHelper.synthesisAndPlay(question.correctAnswer, requireContext()) {
                        btnRead.isEnabled = true
                    }
                }
            }
            "comprehension" -> {
                comprehensionContainer.visibility = View.VISIBLE
                etAnswer.setText("")
                
                // 美化输入框
                etAnswer.apply {
                    setBackgroundResource(R.drawable.green_bg)
                    setPadding(32, 24, 32, 24)
                    hint = "请输入你的答案..."
                    textSize = 16f
                }

                // 美化提交按钮
                btnSubmit.apply {
                    setBackgroundColor(Color.parseColor("#4CAF50"))   // 绿色
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp)
                    ).apply {
                        topMargin = 32
                    }
                }

                btnSubmit.setOnClickListener {
                    val userAnswer = etAnswer.text.toString().trim()
                    if (userAnswer.isEmpty()) {
                        Toast.makeText(requireContext(), "请输入答案", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    btnSubmit.isEnabled = false
                    etAnswer.isEnabled = false
                    Toast.makeText(requireContext(), "正在评分...", Toast.LENGTH_SHORT).show()

                    // 使用混元大模型计算相似度
                    HunyuanHelper.getSimilarity(userAnswer, question.correctAnswer, requireContext()) { similarity ->
                        val baseScore = if (similarity >= 0f) {
                            (similarity * 100).toInt() // 将 0~1 的相似度转换为 0~100 的分数
                        } else {
                            0
                        }

                        Handler(Looper.getMainLooper()).post {
                            btnSubmit.isEnabled = true
                            etAnswer.isEnabled = true
                            Toast.makeText(requireContext(), "得分：$baseScore", Toast.LENGTH_SHORT).show()

                            (activity as? AssessmentActivity)?.saveAnswer(currentIndex, "阅读得分：$baseScore")
                            goToNext(questions, currentIndex + 1)
                        }
                    }
                }
            }
        }
    }

    private fun goToNext(questions: List<AssessmentQuestion>, nextIndex: Int) {
        if (nextIndex < questions.size) {
            (activity as? AssessmentActivity)?.loadFragment(
                QuestionFragment.newInstance(questions, nextIndex)
            )
        } else {
            (activity as? AssessmentActivity)?.onAllQuestionsFinished()
        }
    }
}

// ========== 结果页 ==========
class ResultFragment : Fragment(R.layout.fragment_result) {
    companion object {
        private const val ARG_RESULT = "result"
        fun newInstance(result: AssessmentResult): ResultFragment {
            return ResultFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_RESULT, result) }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val result = arguments?.getSerializable(ARG_RESULT) as? AssessmentResult ?: return

        view.findViewById<TextView>(R.id.tvListeningScore).text = "听力能力：${result.listeningScore.toInt()}"
        view.findViewById<TextView>(R.id.tvExpressionScore).text = "表达能力：${result.expressionScore.toInt()}"
        view.findViewById<TextView>(R.id.tvComprehensionScore).text = "理解能力：${result.comprehensionScore.toInt()}"
        view.findViewById<TextView>(R.id.tvOverallScore).text = "综合评分：${result.overallScore.toInt()}"

        view.findViewById<Button>(R.id.btnFinish).setOnClickListener {
            activity?.finish()
        }
    }
}