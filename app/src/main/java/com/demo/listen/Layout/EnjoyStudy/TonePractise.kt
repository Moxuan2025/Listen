package com.demo.listen.Layout.EnjoyStudy


import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.demo.listen.R

/*
SyllablePractice
    ->
        TonePractise
        |- FragmentPracticeContent
 */
class TonePractise : AppCompatActivity() {

    private lateinit var toneYinPing: TextView      // 阴平
    private lateinit var toneYangPing: TextView     // 阳平
    private lateinit var toneShang: TextView        // 上声
    private lateinit var toneQu: TextView           // 去声
    private lateinit var practiseArea: FrameLayout  // 练习内容区域
    private var practiceContent = FragmentPracticeContent()

    private var tones: List<String> = listOf("-")
    private var pinyin: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tone_practise)

        tones = intent.getStringArrayListExtra("tones")?: listOf("-")
        if (tones.size == 5)
            pinyin = tones[4]

        mapWidget()
        initPage()
        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        toneYinPing = findViewById<TextView>(R.id.tone_yin_ping)
        toneYangPing = findViewById<TextView>(R.id.tone_yang_ping)
        toneShang = findViewById<TextView>(R.id.tone_shang)
        toneQu = findViewById<TextView>(R.id.tone_qu)
        practiseArea = findViewById<FrameLayout>(R.id.tone_practise_area)
        loadFragment(practiceContent)
    }

    private fun initPage() {
        if (tones.size < 4)
            return
        toneYinPing.text = tones[0]
        toneYangPing.text = tones[1]
        toneShang.text = tones[2]
        toneQu.text = tones[3]
    }

    private fun handleClick() {
        toneYinPing.setOnClickListener {
            toneChange(1)
        }
        toneYangPing.setOnClickListener {
            toneChange(2)
        }
        toneShang.setOnClickListener {
            toneChange(3)
        }
        toneQu.setOnClickListener {
            toneChange(4)
        }
    }

    private var curTone: Int = 1    // 1-4
    private fun toneChange(index: Int) {
        if (curTone == index) return

        when (curTone) {
            1 -> {
                toneYinPing.setBackgroundResource(R.drawable.bg_tone_normal)
                toneYinPing.setTextColor(Color.BLACK)
            }
            2 -> {
                toneYangPing.setBackgroundResource(R.drawable.bg_tone_normal)
                toneYangPing.setTextColor(Color.BLACK)
            }
            3 -> {
                toneShang.setBackgroundResource(R.drawable.bg_tone_normal)
                toneShang.setTextColor(Color.BLACK)
            }
            4 -> {
                toneQu.setBackgroundResource(R.drawable.bg_tone_normal)
                toneQu.setTextColor(Color.BLACK)
            }
        }
        curTone = index
        when (index) {
            1 -> {
                toneYinPing.setBackgroundResource(R.drawable.bg_tone_practising)
                toneYinPing.setTextColor(Color.WHITE)
                // TODO: change content
            }
            2 -> {
                toneYangPing.setBackgroundResource(R.drawable.bg_tone_practising)
                toneYangPing.setTextColor(Color.WHITE)
            }
            3 -> {
                toneShang.setBackgroundResource(R.drawable.bg_tone_practising)
                toneShang.setTextColor(Color.WHITE)
            }
            4 -> {
                toneQu.setBackgroundResource(R.drawable.bg_tone_practising)
                toneQu.setTextColor(Color.WHITE)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tone_practise_area, fragment)
            .commit()
    }
}