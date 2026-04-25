package com.demo.listen.Layout.EnjoyStudy

/*
SyllableList    -> SyllablePractice -> SpellPractise
                                    -> TonePractise
 */


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.jvm.java

class SyllablePractice : AppCompatActivity() {

    private lateinit var word: TextView       // 词汇
    private lateinit var phrase: TextView     // 词组
    private lateinit var sentence: TextView   // 句子
    private lateinit var fWrod: TextView        // go->词汇练习
    private lateinit var fPhrase: TextView      // go->词组练习
    private lateinit var fSentence: TextView    // go->句子练习

    private var syllable: String? = null

    private var pinyin: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_syllable_practice)

        mapWidget()

        syllable = intent.getStringExtra("Syllable")
        pinyin = intent.getStringArrayListExtra("Pinyin") ?: emptyList()

        findViewById<TextView>(R.id.tv_syllable_target).text = syllable
        addPinYin(pinyin ?: emptyList())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        word = findViewById<TextView>(R.id.tv_syllable_word)
        phrase = findViewById<TextView>(R.id.tv_syllable_phrase)
        sentence = findViewById<TextView>(R.id.tv_syllable_sentence)
        fWrod = findViewById<TextView>(R.id.word_go)
        fPhrase = findViewById<TextView>(R.id.phrase_go)
        fSentence = findViewById<TextView>(R.id.sentence_go)
    }

    private fun addPinYin(list: List<String>) {
        val gridLayout = findViewById<GridLayout>(R.id.gl_syllable_group)
        var num = "共 ${pinyin?.size.toString()} 个"
        findViewById<TextView>(R.id.syllable_number).text = num

        gridLayout.removeAllViews()
        gridLayout.columnCount = 3  // 3 列

        for ((index, pinyinItem) in list.withIndex()) {
            // 创建 TextView
            val textView = TextView(this).apply {
                text = pinyinItem
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(16, 12, 16, 12)

                setBackgroundResource(R.drawable.input_box)
                setTextColor(Color.BLACK)

                setOnClickListener {
                    // 跳转到其他Activity，并传递拼音
                    val tones = getPinyinTones(this@SyllablePractice, pinyinItem)
                    val intent = Intent(this@SyllablePractice,
                        TonePractise::class.java).apply {
                            putStringArrayListExtra("tones",
                                ArrayList(tones ?: emptyList()))
                    }
                    startActivity(intent)
                }
            }

            // 创建 GridLayout.LayoutParams
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)

                // 设置边距
                setMargins(8, 8, 8, 8)
            }

            gridLayout.addView(textView, params)
        }
    }

    fun getPinyinTones(context: Context, pinyin: String): List<String>? {
        val json = context.assets.open("Tone.json").bufferedReader().use { it.readText() }
        val map = Gson().fromJson<Map<String, List<String>>>(json, object : TypeToken<Map<String, List<String>>>() {}.type)
        return map[pinyin.lowercase()]
    }
}