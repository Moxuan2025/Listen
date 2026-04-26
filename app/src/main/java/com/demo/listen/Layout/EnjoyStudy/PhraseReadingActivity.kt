package com.demo.listen.Layout.EnjoyStudy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.demo.listen.R
import com.demo.listen.data.PhraseQuestion
import com.demo.listen.net.HunyuanHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class PhraseReadingActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var tvPassage: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var etAnswer: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvFeedback: TextView

    private var questions: List<PhraseQuestion> = emptyList()
    private var currentIndex = 0
    private var totalScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phrase_reading)

        bindViews()
        loadQuestions()
        showQuestion(currentIndex)

        btnSubmit.setOnClickListener {
            submitAnswer()
        }
    }

    private fun bindViews() {
        tvProgress = findViewById(R.id.tv_progress)
        scrollView = findViewById(R.id.scrollView)
        tvPassage = findViewById(R.id.tv_passage)
        tvQuestion = findViewById(R.id.tv_question)
        etAnswer = findViewById(R.id.et_answer)
        btnSubmit = findViewById(R.id.btn_submit)
        progressBar = findViewById(R.id.pr_progress_bar)
        tvFeedback = findViewById(R.id.tv_feedback)
    }

    private var questionNum = 0
    private fun loadQuestions() {
        try {
            val inputStream = assets.open("phrase_reading.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<PhraseQuestion>>() {}.type
            questions = Gson().fromJson(reader, type)
            questionNum = questions.size
            progressBar.max = questionNum
            tvProgress.text = "1/${questionNum}"
            inputStream.close()
        } catch (e: Exception) {
            Toast.makeText(this, "题目加载失败", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showQuestion(index: Int) {
        if (index >= questionNum) {
            showFinalResult()
            return
        }

        val q = questions[index]
        progressBar.progress = index + 1
        tvProgress.text = "${index+1}/${questionNum}"
        tvPassage.text = q.passage
        tvQuestion.text = q.question

        // 清空输入框，启用提交按钮
        etAnswer.setText("")
        etAnswer.isEnabled = true
        btnSubmit.isEnabled = true

        // 隐藏加载进度和反馈
        progressBar.visibility = View.INVISIBLE
        tvFeedback.visibility = View.INVISIBLE
        tvFeedback.text = ""

        // 滚动到顶部
        scrollView.scrollTo(0, 0)
    }

    private fun submitAnswer() {
        val userAnswer = etAnswer.text.toString().trim()
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "请输入答案", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载状态
        btnSubmit.isEnabled = false
        etAnswer.isEnabled = false
        progressBar.visibility = View.VISIBLE
        tvFeedback.visibility = View.VISIBLE
        tvFeedback.text = "正在评分..."

        val correctAnswer = questions[currentIndex].answer

        // 调用混元大模型计算相似度
        HunyuanHelper.getSimilarity(userAnswer, correctAnswer, this) { similarity ->
            runOnUiThread {
                progressBar.visibility = View.INVISIBLE

                if (similarity < 0) {
                    // 评分失败
                    Toast.makeText(this, "评分失败，请重试", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    etAnswer.isEnabled = true
                    tvFeedback.visibility = View.INVISIBLE
                } else {
                    // 计算得分
                    val score = (similarity * 100).toInt()
                    totalScore += score

                    // 显示得分
                    tvFeedback.text = "本题得分：$score 分"
                    tvFeedback.visibility = View.VISIBLE

                    // 禁用输入，防止重复提交
                    btnSubmit.isEnabled = false
                    etAnswer.isEnabled = false

                    // 延时进入下一题
                    Handler(Looper.getMainLooper()).postDelayed({
                        nextQuestion()
                    }, 2000)
                }
            }
        }
    }

    private fun nextQuestion() {
        currentIndex++
        if (currentIndex < questionNum) {
            showQuestion(currentIndex)
        } else {
            showFinalResult()
        }
    }

    private fun showFinalResult() {
        val maxScore = questionNum * 100
        AlertDialog.Builder(this)
            .setTitle("练习完成")
            .setMessage("全部完成！总得分：$totalScore/$maxScore")
            .setPositiveButton("确定") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
