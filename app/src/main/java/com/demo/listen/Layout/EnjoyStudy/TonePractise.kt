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
import androidx.lifecycle.ViewModelProvider
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

    private lateinit var viewModel: SharePracticeData

    private var tones: List<String> = listOf("-")
    private var pinyin: String = ""
    private var syllable: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tone_practise)

        syllable = intent.getStringExtra("Syllable") ?: "<None>"
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

    private val isInit: Boolean = true
    private fun initPage() {
        viewModel = ViewModelProvider(this@TonePractise)[SharePracticeData::class.java]

        if (tones.size < 4)
            return
        toneYinPing.text = tones[0]
        toneYangPing.text = tones[1]
        toneShang.text = tones[2]
        toneQu.text = tones[3]

        viewModel.changeAction(getAction())
        viewModel.setNext("word")
        viewModel.changeIndex(0)
        viewModel.changeTarget(tones.take(4))
        viewModel.index.observe(this) { index ->
            toneChange(index)
        }
    }

    private fun getAction(): List<String> {
        // TODO: 从服务器获取发声动作数据，根据拼音 pinyin
        return testAction;
    }

    private fun handleClick() {
        toneYinPing.setOnClickListener {
            viewModel.changeIndex(0)
        }
        toneYangPing.setOnClickListener {
            viewModel.changeIndex(1)
        }
        toneShang.setOnClickListener {
            viewModel.changeIndex(2)
        }
        toneQu.setOnClickListener {
            viewModel.changeIndex(3)
        }
    }

    private var curTone: Int = 0    // 0-3
    private fun toneChange(index: Int) {
        if (curTone == index) return

        when (curTone) {
            0 -> {
                toneYinPing.setBackgroundResource(R.drawable.bg_tone_normal)
                toneYinPing.setTextColor(Color.BLACK)
            }
            1 -> {
                toneYangPing.setBackgroundResource(R.drawable.bg_tone_normal)
                toneYangPing.setTextColor(Color.BLACK)
            }
            2 -> {
                toneShang.setBackgroundResource(R.drawable.bg_tone_normal)
                toneShang.setTextColor(Color.BLACK)
            }
            3 -> {
                toneQu.setBackgroundResource(R.drawable.bg_tone_normal)
                toneQu.setTextColor(Color.BLACK)
            }
        }
        curTone = index
        when (index) {
            0 -> {
                toneYinPing.setBackgroundResource(R.drawable.bg_tone_practising)
                toneYinPing.setTextColor(Color.WHITE)
                // TODO: change content
            }
            1 -> {
                toneYangPing.setBackgroundResource(R.drawable.bg_tone_practising)
                toneYangPing.setTextColor(Color.WHITE)
            }
            2 -> {
                toneShang.setBackgroundResource(R.drawable.bg_tone_practising)
                toneShang.setTextColor(Color.WHITE)
            }
            3 -> {
                toneQu.setBackgroundResource(R.drawable.bg_tone_practising)
                toneQu.setTextColor(Color.WHITE)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val bundle = Bundle().apply {
            putString("Syllable", syllable)
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.tone_practise_area, fragment)
            .commit()
    }

    var testAction = listOf<String>(
        "bā: 双唇紧闭，舌尖抵下齿，软腭上升挡住鼻腔。然后双唇突然张开，气流爆发而出，声带不振动。唇形保持圆拢，发出高平调“巴”音。嘴巴动作干脆利落",
        "bá: 动作同上，但声带振动，气息更强。发音时喉部稍微用力，声音上扬（二声）。双唇张开瞬间，舌根略抬，口腔共鸣增强，类似“拔”但短促有力",
        "bǎ: 先做闭唇蓄气，然后双唇打开，同时舌尖轻抵下齿背。声带振动，音调先降后升（三声）。发音时下巴微微下拉，口腔稍开，像“把”字前的准备停顿",
        "bà: 双唇紧闭后突然弹开，气流爆发干脆。声带全程振动，音调高而短促下降（四声）。喉部收紧，腹部快速送气，唇形由圆变扁，如“爸”的结尾干脆收束"
    )
}