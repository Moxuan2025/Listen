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
        viewModel.changeIndex(0)
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
//            toneChange(1)
            viewModel.changeIndex(0)
        }
        toneYangPing.setOnClickListener {
//            toneChange(2)
            viewModel.changeIndex(1)
        }
        toneShang.setOnClickListener {
//            toneChange(3)
            viewModel.changeIndex(2)
        }
        toneQu.setOnClickListener {
//            toneChange(4)
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
            putString("pinyin", pinyin)
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.tone_practise_area, fragment)
            .commit()
    }

    var testAction = listOf<String>(
        "b: 双唇紧闭，阻碍气流，双唇突然放开，轻运气，声带不振动\nbā: 双唇紧闭憋气->轻爆破开唇->张大嘴发a，连贯滑动放",
        "bá: 咕咕\n嘎嘎",
        "bǎ: 嘎嘎\n咕咕",
        "bà: \nb: 开唇轻闭爆破，不运气\nà: 大口开扇，舌放平"
    )
}