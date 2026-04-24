package com.demo.listen.Layout.Assessment

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R

class Assess : AppCompatActivity() {

    private lateinit var btNext: Button
    private lateinit var problemType: TextView
    private lateinit var progressCounter: TextView
    private lateinit var progress: ProgressBar
    private lateinit var answer: TextView
    private lateinit var tip: TextView
    private lateinit var playSound: LinearLayout
    private lateinit var playSoundIB: ImageButton
    private lateinit var problemArea: FrameLayout


    private var problemTotalNum = 15
    private var index = 0

    private var level = 4           // 残疾程度
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assess)

        level = intent.getIntExtra("difficult", 4)
        mapWidget()
        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        btNext = findViewById<Button>(R.id.assess_next)
        problemType = findViewById<TextView>(R.id.assess_problem_type)
        progressCounter = findViewById<TextView>(R.id.assess_problem_counter)
        progress = findViewById<ProgressBar>(R.id.progress_bar)
        answer = findViewById<TextView>(R.id.assess_answer)
        tip = findViewById<TextView>(R.id.assess_tip)
        playSound = findViewById<LinearLayout>(R.id.assess_play_sound)
        playSoundIB = findViewById<ImageButton>(R.id.assess_play_sound_ib)
        problemArea = findViewById<FrameLayout>(R.id.assess_problem_area)
    }

    private fun handleClick() {
        btNext.setOnClickListener {

        }
        playSound.setOnClickListener {
            getPlaySound()
        }
        playSoundIB.setOnClickListener {
            getPlaySound()
        }
    }

    private fun getPlaySound() {
        tip.text = "播放中"
        // TODO: get and play sound
        tip.text = "重新播放"
    }
}