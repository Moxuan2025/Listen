package com.demo.listen.Layout.EnjoyStudy

import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class NormalStudy : AppCompatActivity() {

    private val practiceContent = FragmentPracticeContent()

    private lateinit var progressBar: ProgressBar
    private lateinit var progressHint: TextView

    private lateinit var viewModel: SharePracticeData
    private lateinit var pinyinList: ArrayList<String>
    private var mode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_normal_study)

        mode = intent.getStringExtra("Mode") ?: ""
        pinyinList = intent.getStringArrayListExtra("PinYinList") ?: arrayListOf()
        Toast.makeText(this, pinyinList.size.toString(), Toast.LENGTH_SHORT).show()

        mapWidget()
        initPage()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        progressBar = findViewById<ProgressBar>(R.id.ns_progress_bar)
        progressHint = findViewById<TextView>(R.id.ns_progress_hit)
    }


    private var itemNum = 0
    private fun initPage() {
        if (mode.isEmpty()) finish()
        loadFragment(practiceContent)

        viewModel = ViewModelProvider(this@NormalStudy)[SharePracticeData::class.java]

        var title = findViewById<TextView>(R.id.normal_study_title)
        when(mode) {
            "word" -> title.text = "词汇练习"
            "phrase" -> title.text = "词组练习"
            "sentence" -> title.text = "句子练习"
            "test" -> title.text = "测试"
        }

        var wpy = loadWordPinYinFromJson(this)
        itemNum = wpy.size
        progressBar.max = itemNum
        progressBar.progress = 0
        progressHint.text = "1/${itemNum}"

        viewModel.setWordPinYin(wpy)
        viewModel.setType("word")
        viewModel.setNext("report")
        viewModel.changeIndex(0)
        viewModel.index.observe(this) { index ->
            progressBar.progress = index + 1
            progressHint.text = "${index+1}/${itemNum}"
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.normal_study_content, fragment)
            .commit()
    }

    fun loadWordPinYinFromJson(context: Context, fileName: String = "ba.json"): List<WordPinYin> {
        val result = mutableListOf<WordPinYin>()

        try {
            // 读取 assets 中的 JSON 文件
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val jsonString = reader.use { it.readText() }

            // 解析 JSON
            val gson = Gson()
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            val jsonMap: Map<String, List<String>> = gson.fromJson(jsonString, type)

            // 获取 "ba" 数组中的词汇列表
            val wordList = jsonMap["ba"] ?: emptyList()

            // 遍历每个词汇，构建 WordPinYin 对象
            for (word in wordList) {
                val wordData = jsonMap[word] ?: listOf("", "")

                val pinyin = wordData.getOrElse(0) { "" }
                val actionText = wordData.getOrElse(1) { "" }

                result.add(
                    WordPinYin(
                        word = word,
                        pinyin = pinyin,
                        action = actionText
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}