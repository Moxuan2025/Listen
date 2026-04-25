package com.demo.listen.Layout.Assessment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.demo.listen.R
import com.demo.listen.net.TencentSpeechHelper
import android.os.Handler
import android.os.Looper
class AssessmentActivity : AppCompatActivity() {

    private lateinit var childUsername: String
    private var sessionId: String? = null
    private val answers = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        childUsername = intent.getStringExtra("child_username") ?: ""

        // 直接从 DisabilityLevel 获取等级，开始答题
        val level = intent.getIntExtra("level", 1)
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
        sessionId = "mock-session-${System.currentTimeMillis()}"
        val questions = generateMockQuestions()
        loadFragment(QuestionFragment.newInstance(questions, 0))
    }

    fun saveAnswer(questionIndex: Int, answer: String) {
        answers[questionIndex] = answer
    }

    fun onAllQuestionsFinished() {
        val result = AssessmentResult(
            listeningScore = (70..90).random().toFloat(),
            expressionScore = (60..80).random().toFloat(),
            comprehensionScore = (65..85).random().toFloat(),
            overallScore = (65..85).random().toFloat()
        )
        loadFragment(ResultFragment.newInstance(result))
    }

    private fun generateMockQuestions(): List<AssessmentQuestion> {
        val list = mutableListOf<AssessmentQuestion>()
        val wordOptions = listOf("苹果", "香蕉", "橘子", "西瓜")
        for (i in 0..4) {
            list.add(
                AssessmentQuestion(
                    index = i, type = "word_choice",
                    content = "请选出正确的词语：${wordOptions[i % wordOptions.size]}",
                    options = wordOptions,
                    correctAnswer = wordOptions[i % wordOptions.size],
                    audioUrl = null
                )
            )
        }
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
        val texts = listOf("短文第一段", "短文第二段", "短文第三段", "短文第四段", "短文第五段")
        for (i in 10..14) {
            list.add(
                AssessmentQuestion(
                    index = i, type = "comprehension",
                    content = "阅读以下短文并回答：${texts[i - 10]}\n问题：文章主要讲了什么？",
                    options = null,
                    correctAnswer = "参考答案",
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

// ========== SelectLevelFragment ==========
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

// ========== QuestionFragment（核心） ==========
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


    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val questions =
            arguments?.getSerializable(ARG_QUESTIONS) as? List<AssessmentQuestion> ?: return
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

        // 题目显示逻辑：词语选择题隐藏，其他显示原文
        if (question.type == "word_choice") {
            tvQuestion.text = "？"
        } else {
            tvQuestion.text = question.content
        }

        // 默认隐藏所有交互控件
        optionsContainer.visibility = View.GONE
        btnRead.visibility = View.GONE
        comprehensionContainer.visibility = View.GONE

        when (question.type) {
            "word_choice" -> {
                optionsContainer.visibility = View.VISIBLE
                btnRead.visibility = View.VISIBLE
                btnRead.text = "听题目"

                optionsContainer.removeAllViews()
                val optionButtons = mutableListOf<Button>() // 保存按钮引用
                question.options?.forEach { option ->
                    val btn = Button(requireContext()).apply {
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

                // 为每个按钮设置点击事件
                optionButtons.forEach { btn ->
                    btn.setOnClickListener {
                        if (answered) return@setOnClickListener
                        answered = true

                        // 禁用所有按钮
                        optionButtons.forEach { it.isEnabled = false }
                        btnRead.isEnabled = false

                        // 先全部重置背景（保留默认）
                        optionButtons.forEach { it.setBackgroundResource(android.R.drawable.btn_default) }

                        // 用户选中 -> 红色
                        btn.setBackgroundColor(0xFFFF0000.toInt()) // 红色

                        // 正确答案 -> 绿色（覆盖红色，如果重合则绿色优先）
                        optionButtons.firstOrNull { it.text == question.correctAnswer }?.let {
                            it.setBackgroundColor(0xFF00AA00.toInt()) // 绿色
                        }

                        // 保存答案并延时跳转
                        (activity as? AssessmentActivity)?.saveAnswer(
                            currentIndex,
                            btn.text.toString()
                        )
                        Handler(Looper.getMainLooper()).postDelayed({
                            goToNext(questions, currentIndex + 1)
                        }, 1000)
                    }
                }
            }

            "read_after" -> {
                // ... 保持不变，你之前的跟读代码 ...
                btnRead.visibility = View.VISIBLE
                btnRead.text = "听示范音"

                val recordBtn = Button(requireContext()).apply {
                    text = "🎤 录音"
                    setOnClickListener {
                        Toast.makeText(requireContext(), "录音功能待实现", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                (btnRead.parent as? ViewGroup)?.addView(recordBtn)

                btnRead.setOnClickListener {
                    btnRead.isEnabled = false
                    TencentSpeechHelper.synthesisAndPlay(question.correctAnswer, requireContext()) {
                        btnRead.isEnabled = true
                        (activity as? AssessmentActivity)?.saveAnswer(currentIndex, "已跟读")
                        goToNext(questions, currentIndex + 1)
                    }
                }
            }

            "comprehension" -> {
                // ... 保持不变 ...
                comprehensionContainer.visibility = View.VISIBLE
                etAnswer.setText("")

                btnSubmit.setOnClickListener {
                    val userAnswer = etAnswer.text.toString().trim()
                    (activity as? AssessmentActivity)?.saveAnswer(currentIndex, userAnswer)
                    goToNext(questions, currentIndex + 1)
                }
            }
        }
    }

    // goToNext 方法不变
    private fun goToNext(questions: List<AssessmentQuestion>, nextIndex: Int) {
        if (nextIndex < questions.size) {
            (activity as? AssessmentActivity)?.loadFragment(
                QuestionFragment.newInstance(
                    questions,
                    nextIndex
                )
            )
        } else {
            (activity as? AssessmentActivity)?.onAllQuestionsFinished()
        }
    }
}






// ========== ResultFragment ==========
class ResultFragment : Fragment(R.layout.fragment_result) {
    companion object {
        private const val ARG_RESULT = "result"

        fun newInstance(result: AssessmentResult): ResultFragment {
            return ResultFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_RESULT, result)
                }
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