package com.demo.listen.Layout.EnjoyStudy


import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.listen.R
import com.demo.listen.data.WordQuestion
import com.demo.listen.net.TencentSpeechHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class WordListeningActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var btnPlay: ImageButton
    private lateinit var optionsContainer: LinearLayout
    private lateinit var tvScore: TextView
    private lateinit var tvHint: TextView

    private var questions: List<WordQuestion> = emptyList()
    private var currentIndex = 0
    private var totalScore = 0

    private var isPlaying = false
    private var canAnswer = false   // 是否允许点击选项（播放结束后才可）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_listening)

        tvProgress = findViewById(R.id.tv_progress)
        btnPlay = findViewById(R.id.btn_play)
        optionsContainer = findViewById(R.id.options_container)
        tvScore = findViewById(R.id.tv_score)

        progressBar = findViewById<ProgressBar>(R.id.wl_progress_bar)
        tvHint = findViewById(R.id.tv_hint)

        loadQuestions()
        showQuestion(currentIndex)

        btnPlay.setOnClickListener {
            playCurrentWord()
        }
    }

    private var questionNum =0
    private fun loadQuestions() {
        try {
            val inputStream = assets.open("word_listening.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<WordQuestion>>() {}.type
            questions = Gson().fromJson(reader, type)
            questionNum = questions.size
            progressBar.max = questionNum
            tvProgress.text = "${1}/${questionNum}"
            inputStream.close()
        } catch (e: Exception) {
            Toast.makeText(this, "题目加载失败", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showQuestion(index: Int) {
        if (index >= questionNum) {
            // 结束
            Toast.makeText(this, "完成！总分：$totalScore/${questionNum}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val q = questions[index]
        tvProgress.text = "${index+1}/${questionNum}"
        progressBar.progress = index

        tvScore.text = "得分：$totalScore"
        tvHint.text = "听发音，选择正确的词汇"

        optionsContainer.removeAllViews()
        canAnswer = false

        // 打乱选项并创建 TextView 按钮（参考评估阶段样式）
        val shuffledOptions = q.options.shuffled()
        for (option in shuffledOptions) {
            val optionView = TextView(this).apply {
                text = option
                tag = option
                textSize = 20f
                setTextColor(resources.getColor(android.R.color.black, null))
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.bg_choice)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp)
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
                setOnClickListener { onOptionSelected(this, q) }
            }
            optionsContainer.addView(optionView)
        }

        // 自动播放一次
        playCurrentWord()
    }

    private fun playCurrentWord() {
        if (isPlaying) return
        val q = questions.getOrNull(currentIndex) ?: return
        isPlaying = true
        btnPlay.isEnabled = false
        TencentSpeechHelper.synthesisAndPlay(
            text = q.word,
            context = this,
            onComplete = {
                isPlaying = false
                btnPlay.isEnabled = true
                canAnswer = true
                tvHint.text = "请选择一个选项"
            }
        )
    }

    private fun onOptionSelected(optionView: TextView, question: WordQuestion) {
        if (!canAnswer) {
            Toast.makeText(this, "请先听完整发音", Toast.LENGTH_SHORT).show()
            return
        }

        canAnswer = false
        val selected = optionView.tag as String
        val correct = question.correct

        // 先禁用所有选项
        for (i in 0 until optionsContainer.childCount) {
            optionsContainer.getChildAt(i).isEnabled = false
        }

        // 设置颜色（参考评估阶段样式）
        for (i in 0 until optionsContainer.childCount) {
            val view = optionsContainer.getChildAt(i) as TextView
            if (view.tag == correct) {
                view.setBackgroundResource(R.drawable.bg_choice_selected)
            } else if (view.tag == selected && selected != correct) {
                view.setBackgroundColor(0xFFFF0000.toInt())
            }
        }

        if (selected == correct) {
            totalScore++
            tvScore.text = "得分：$totalScore"
        }

        // 延时进入下一题
        Handler(Looper.getMainLooper()).postDelayed({
            currentIndex++
            showQuestion(currentIndex)
        }, 1500)
    }

    override fun onDestroy() {
        super.onDestroy()
        TencentSpeechHelper.release()
    }
}
