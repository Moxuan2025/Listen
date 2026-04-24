package com.demo.listen.Layout.Assessment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.Layout.DataType.BinaryChoice
import com.demo.listen.Layout.DataType.CompleteSentence
import com.demo.listen.Layout.DataType.Problem
import com.demo.listen.Layout.DataType.SimpleProblem
import com.demo.listen.Layout.DataType.WordFormation
import com.demo.listen.R
import java.io.File

class Assess : AppCompatActivity() {

    private lateinit var btSure: Button
    private lateinit var problemType: TextView
    private lateinit var progressCounter: TextView
    private lateinit var progress: ProgressBar
    private lateinit var answer: TextView
    private lateinit var tip: TextView
    private lateinit var problemTxt: TextView
    private lateinit var playSound: LinearLayout
    private lateinit var playSoundIB: ImageButton
    private lateinit var problemArea: FrameLayout
    private lateinit var fRecord: LinearLayout
    private lateinit var record: ImageView

    private val fBinaryChoice = FragmentBinaryChoice()
    private val fWordFormation = FragmentWordFormation()


    private var problemTotalNum = 15
    private var index = 0
    private var state: String = "not_answer_yet"    // 三种状态：answered, not_answer_yet, answer_showed
    private lateinit var viewModel: ShareListenState    // 没有播放过音频不能做选择

    private var level = 4           // 残疾程度
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assess)

        level = intent.getIntExtra("level", 4)
        mapWidget()
        initPage()
        handleClick()
        btSure.visibility = View.INVISIBLE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        viewModel = ViewModelProvider(this@Assess)[ShareListenState::class.java]

        btSure = findViewById<Button>(R.id.assess_sure)
        problemType = findViewById<TextView>(R.id.assess_problem_type)
        progressCounter = findViewById<TextView>(R.id.assess_problem_counter)
        progress = findViewById<ProgressBar>(R.id.progress_bar)
        answer = findViewById<TextView>(R.id.assess_answer)
        tip = findViewById<TextView>(R.id.assess_tip)
        problemTxt = findViewById<TextView>(R.id.assess_problem_txt)
        playSound = findViewById<LinearLayout>(R.id.assess_play_sound)
        playSoundIB = findViewById<ImageButton>(R.id.assess_play_sound_ib)
        problemArea = findViewById<FrameLayout>(R.id.assess_problem_area)
        fRecord = findViewById<LinearLayout>(R.id.frame_record)
        record = findViewById<ImageView>(R.id.assess_sound_record)
    }

    private var isInit = true
    @SuppressLint("ResourceAsColor")
    private fun initPage() {
        loadFragment(fBinaryChoice)
        problemTxt.text = ""
        viewModel.choice.observe(this) { choice ->
            if (isInit) {
                isInit = false
                return@observe
            }
            state = "answered"
            btSure.visibility = View.VISIBLE
        }
        viewModel.updateOptions(sigleSelection[index].optionList())
    }

    private fun handleClick() {
        btSure.setOnClickListener {
            if (state == "answered") {
                answer.text = sigleSelection[index].getAnswer()
                if (index+1 == problemTotalNum)
                    btSure.text = "查看报告"
                else
                    btSure.text = "下一个"
                state = "answer_showed"

                ObjectAnimator.ofInt(progress, "progress",
                    index, index+1).apply {
                    duration = 100
                    start()
                }
            } else
                goNext()
        }
        playSound.setOnClickListener {
            getPlaySound()
        }
        playSoundIB.setOnClickListener {
            getPlaySound()
        }

        record.setOnTouchListener { _, event ->
            // TODO: 记录录音
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    record.setImageResource(R.drawable.ic_record) // 需要添加录音中的图标
                    record.alpha = 0.7f // 改变透明度表示按下状态
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    record.setImageResource(R.drawable.ic_record_gray)
                    record.alpha = 1.0f
                    state = "answered"
                    btSure.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    private fun getPlaySound() {
        tip.text = "播放中"
        // TODO: get and play sound
        tip.text = "重新播放"

        viewModel.updatePlayState(true)    // 可以做选择了
    }

    // test

    private fun goNext() {
        index++

        // 两种状态：answered, not_answer_yet
        if (state == "not_answer_yet") {    // 还未回答问题，不允许下一题
            Toast.makeText(this@Assess, "您还没有回答问题哦!",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (index == problemTotalNum && state == "answer_showed") {  // 完成所有评估
            startActivity(Intent(this@Assess,
                AssessReport::class.java).apply {
//                putExtra("choice", ArrayList(choice))
//                putExtra("target", ArrayList(target))
            })
            finish()
        }

        state = "not_answer_yet"                   // 更新状态
        viewModel.updatePlayState(false)    // 不能做选择
        progressCounter.text = "${index+1}/${problemTotalNum}"

        answer.text = "?"
        btSure.text = "确定"
        btSure.visibility = View.INVISIBLE

        when (index) {
            6 -> {
                problemType.text = "完成句子"
            }
            9 -> {
                problemTxt.text = ""
                removeCurrentFragment()
                fRecord.visibility = View.VISIBLE
                problemType.text = "跟读"
            }
            12 -> {
                fRecord.visibility = View.GONE
                loadFragment(fWordFormation)
                problemType.text = "选字组词"
            }
        }

        when (index) {
            0,1,2 -> {
                tip.text = "听录音，选择正确的读音"
                viewModel.updateOptions(sigleSelection[index].optionList())
            }
            3,4,5 -> {
                tip.text = "听录音，选择正确的词语"
                viewModel.updateOptions(sigleSelection[index].optionList())
            }
            6,7,8 -> {
                problemTxt.text = sigleSelection[index].getProblem()
                tip.text = "听录音，选择正确的词语"
                viewModel.updateOptions(sigleSelection[index].optionList())
            }
            9,10,11 -> {
                tip.text = "听录音，读出正确的读音"
                viewModel.updateOptions(sigleSelection[index].optionList())
                // TODO: 录音
            }
            else -> {
                tip.text = "听录音，选字组词"
                viewModel.updateOptions(sigleSelection[index].optionList())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.assess_problem_area, fragment)
            .commit()
    }

    private fun removeCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.assess_problem_area)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    val sigleSelection: List<Problem> = listOf(
        BinaryChoice("wu", "hu"),       //读音
        BinaryChoice("gu", "ga"),
        BinaryChoice("cong", "zong"),
        BinaryChoice("ca", "che"),      // 词语
        BinaryChoice("he", "ha"),
        BinaryChoice("ba", "bu"),

        CompleteSentence("今天___很高兴", "我", "你"),   // 完成句子
        CompleteSentence("红红的___像火球", "太阳", "月球"),
        CompleteSentence("吃___很棒", "肉", "饭"),

        SimpleProblem("苹果"),       // 跟读
        SimpleProblem("书籍"),
        SimpleProblem("乌龟"),

        WordFormation("学校", listOf("学","习", "生", "校")),       // 组词. 多选
        WordFormation("学校", listOf("学","习", "生", "校")),
        WordFormation("学校", listOf("学","习", "生", "校")),
    )
}