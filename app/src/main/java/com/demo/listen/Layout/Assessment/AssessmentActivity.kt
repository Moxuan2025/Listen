package com.demo.listen.Layout.Assessment

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.demo.listen.R
import android.widget.TextView
import android.widget.LinearLayout
import android.view.ViewGroup

class AssessmentActivity : AppCompatActivity() {

    private lateinit var childUsername: String
    private var sessionId: String? = null
    private val answers = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        childUsername = intent.getStringExtra("child_username") ?: ""

        if (savedInstanceState == null) {
            loadFragment(SelectLevelFragment())
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.assessment_container, fragment)
            .commit()
    }

    @Suppress("UNUSED_PARAMETER")
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
        for (i in 0..4) {
            list.add(
                AssessmentQuestion(i, "single_choice", "单选题目 $i", listOf("A", "B", "C"), "A", null)
            )
        }
        for (i in 5..9) {
            list.add(
                AssessmentQuestion(i, "read_after", "跟读题目 ${i - 5}", null, "正确文本", "audio_mock_url")
            )
        }
        for (i in 10..14) {
            list.add(
                AssessmentQuestion(i, "comprehension", "理解题目 ${i - 10}", listOf("X", "Y", "Z"), "Y", null)
            )
        }
        return list
    }
}

// ---------- 数据类（实现 Serializable） ----------
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

// ---------- Fragment 占位实现 ----------
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

//class QuestionFragment : Fragment(R.layout.fragment_question) {
//    companion object {
//        private const val ARG_QUESTIONS = "questions"
//        private const val ARG_INDEX = "current_index"
//
//        fun newInstance(questions: List<AssessmentQuestion>, currentIndex: Int): QuestionFragment {
//            return QuestionFragment().apply {
//                arguments = Bundle().apply {
//                    putSerializable(ARG_QUESTIONS, ArrayList(questions))
//                    putInt(ARG_INDEX, currentIndex)
//                }
//            }
//        }
//    }
//
//    @Suppress("DEPRECATION", "UNCHECKED_CAST")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val questions = arguments?.getSerializable(ARG_QUESTIONS) as? List<AssessmentQuestion> ?: return
//        val currentIndex = arguments?.getInt(ARG_INDEX, 0) ?: 0
//        val question = questions[currentIndex]
//
//        when (question.type) {
//            "single_choice", "comprehension" -> {
//                (activity as? AssessmentActivity)?.saveAnswer(currentIndex, "A")
//                goToNext(questions, currentIndex + 1)
//            }
//            "read_after" -> {
//                (activity as? AssessmentActivity)?.saveAnswer(currentIndex, "mock_read")
//                goToNext(questions, currentIndex + 1)
//            }
//        }
//    }
//
//    private fun goToNext(questions: List<AssessmentQuestion>, nextIndex: Int) {
//        if (nextIndex < questions.size) {
//            (activity as? AssessmentActivity)?.loadFragment(
//                QuestionFragment.newInstance(questions, nextIndex)
//            )
//        } else {
//            (activity as? AssessmentActivity)?.onAllQuestionsFinished()
//        }
//    }
//}

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
        val questions = arguments?.getSerializable(ARG_QUESTIONS) as? List<AssessmentQuestion> ?: return
        val currentIndex = arguments?.getInt(ARG_INDEX, 0) ?: 0
        val question = questions[currentIndex]

        val tvProgress = view.findViewById<TextView>(R.id.tvProgress)
        val tvQuestion = view.findViewById<TextView>(R.id.tvQuestion)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.optionsContainer)
        val btnRead = view.findViewById<Button>(R.id.btnRead)

        tvProgress.text = "${currentIndex + 1}/${questions.size}"
        tvQuestion.text = question.content

        // 根据题目类型显示不同界面
        when (question.type) {
            "single_choice", "comprehension" -> {
                optionsContainer.visibility = View.VISIBLE
                btnRead.visibility = View.GONE
                optionsContainer.removeAllViews()
                question.options?.forEach { option ->
                    val btn = Button(requireContext()).apply {
                        text = option
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = 8 }
                        setOnClickListener {
                            // 保存答案并跳到下一题
                            (activity as? AssessmentActivity)?.saveAnswer(currentIndex, option)
                            goToNext(questions, currentIndex + 1)
                        }
                    }
                    optionsContainer.addView(btn)
                }
            }
            "read_after" -> {
                optionsContainer.visibility = View.GONE
                btnRead.visibility = View.VISIBLE
                btnRead.setOnClickListener {
                    // 模拟跟读录音，直接保存固定答案
                    (activity as? AssessmentActivity)?.saveAnswer(currentIndex, "mock_read")
                    goToNext(questions, currentIndex + 1)
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

//class ResultFragment : Fragment(R.layout.fragment_result) {
//    companion object {
//        private const val ARG_RESULT = "result"
//
//        fun newInstance(result: AssessmentResult): ResultFragment {
//            return ResultFragment().apply {
//                arguments = Bundle().apply {
//                    putSerializable(ARG_RESULT, result)
//                }
//            }
//        }
//    }
//
//    @Suppress("DEPRECATION")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val result = arguments?.getSerializable(ARG_RESULT) as? AssessmentResult ?: return
//        // TODO: 显示结果 UI
//    }
//}
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
            activity?.finish()  // 返回上一页（MainActivity）
        }
    }
}