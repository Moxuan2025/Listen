package com.demo.listen.Layout.Assessment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.MainActivity
import com.demo.listen.R
import kotlin.jvm.java
import kotlin.random.Random

class SoundAssessPractice : AppCompatActivity() {

    private lateinit var target: MutableList<String>  // 目标 / 答案
    private lateinit var disturb: MutableList<String> // 干扰
    private lateinit var choice: MutableList<String>  // 用户选择
    private var index: Int = 0                  // 当前题目，代码中指向下一个题目

    private lateinit var tvTarget: TextView
    private lateinit var tvCounter: TextView
    private lateinit var tvChoiceA: TextView
    private lateinit var tvChoiceB: TextView
    private lateinit var btNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sound_assess_practice)

        testInit()
        mapWidget()
        handleFeature()
        handleClick()
        nextProblem()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        tvTarget = findViewById<TextView>(R.id.tv_assess_target)
        tvCounter = findViewById<TextView>(R.id.tv_assess_counter)
        tvChoiceA = findViewById<TextView>(R.id.choice_a)
        tvChoiceB = findViewById<TextView>(R.id.choice_b)
        btNext = findViewById<Button>(R.id.bt_assess_next)
    }

    // 评估？ 训练？
    private fun handleFeature() {
        var feature = intent.getStringExtra("feature")
        when(feature) {
            "Assess" ->     tvTarget.text = "听能评估"
            "Practice" ->   tvTarget.text = "听能训练"
        }
    }

    // 点击处理：点击选项、下一个按钮
    private fun handleClick() {
        tvChoiceA.setOnClickListener {  // 点击了选项A
            tvTarget.visibility = View.VISIBLE
            btNext.visibility = View.VISIBLE
            choice.add(tvChoiceA.text.toString())   // 记录选择
        }
        tvChoiceB.setOnClickListener {  // 点击了选项A
            tvTarget.visibility = View.VISIBLE
            btNext.visibility = View.VISIBLE
            choice.add(tvChoiceB.text.toString())   // 记录选择
        }
        btNext.setOnClickListener {     // 点击了 "下一个"
            if (index != target.size)
                nextProblem()
            else {
                startActivity(Intent(this@SoundAssessPractice,
                    AssessReport::class.java).apply {
                    putExtra("choice", ArrayList(choice))
                    putExtra("target", ArrayList(target))
                })
                finish()
            }
        }
    }

    private fun nextProblem() {
        if (index == target.size)
        {
            // TODO: finish, send data
            val intent = Intent(this, MainActivity::class.java)
            // 不回去
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        tvTarget.text = target[index]
        var real = Random.nextInt(2)
        if (real == 0) {
            tvChoiceA.text = target[index]
            tvChoiceB.text = disturb[index]
        } else {
            tvChoiceB.text = target[index]
            tvChoiceA.text = disturb[index]
        }
        tvTarget.visibility = View.INVISIBLE
        btNext.visibility = View.INVISIBLE

        index++
        if (index == target.size) {
            btNext.setBackgroundColor(Color.YELLOW)
            btNext.text = "查看报告"
            btNext.setTextColor(Color.BLACK)
        }
        tvCounter.text = "${index} / ${target.size}"
    }

    private fun testInit() {
        target = mutableListOf("b", "p")
        disturb = mutableListOf("p", "b")
        choice = mutableListOf()  // 初始化 choice 列表！
        index = 0
    }
}