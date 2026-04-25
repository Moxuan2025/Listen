package com.demo.listen.Layout.EnjoyStudy

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.DataType.PracticeItem
import com.demo.listen.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class PracticeList : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var chooseAll: CheckBox
    private lateinit var list: GridLayout
    private lateinit var start: TextView

    private var practiceItems: List<PracticeItem> = emptyList()
    private val selectedItems = mutableSetOf<Int>() // 记录选中的位置

    private var syllable: String = ""
    private var mode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practice_list)

        syllable = intent.getStringExtra("Syllable") ?: "<None>"
        mode = intent.getStringExtra("mode") ?: "<None>"
        Toast.makeText(this, syllable,
            Toast.LENGTH_SHORT).show()


        mapWidget()
        initPage()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        title = findViewById<TextView>(R.id.practice_list_title)
        chooseAll = findViewById<CheckBox>(R.id.practice_list_choose_all)
        list = findViewById<GridLayout>(R.id.practice_grid_layout)
        start = findViewById<TextView>(R.id.practice_list_start)
    }

    private fun initPage() {
        when (mode) {
            "word"      -> {
                title.text = "词汇练习"
            }
            "phrase"    -> title.text = "词组练习"
            "sentence"  -> {
                title.text = "句子练习"
            }
        }

        practiceItems = loadPracticeItems(this)
        list.columnCount = 1
        setupList()
    }

    private fun setupList() {
        practiceItems.forEachIndexed { index, item ->
            val textView = TextView(this).apply {
                text = item.formatString()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.MATCH_PARENT
                    height = resources.getDimensionPixelSize(R.dimen._40sdp)

                    setMargins(     // 增加间距
                        0,  // left
                        resources.getDimensionPixelSize(R.dimen._4sdp),  // top
                        0,  // right
                        resources.getDimensionPixelSize(R.dimen._4sdp)   // bottom
                    )
                }
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(
                    resources.getDimensionPixelSize(R.dimen._16sdp),
                    0, 0, 0
                )
                textSize = resources.getDimension(R.dimen._18sdp) / resources.displayMetrics.density
                setTextColor(resources.getColor(R.color.white, null))
                background = resources.getDrawable(R.drawable.green_bg, null)

                setOnClickListener {
                    if (selectedItems.contains(index)) {
                        // 取消选中
                        selectedItems.remove(index)
                        background = resources.getDrawable(R.drawable.green_bg, null)
                    } else {
                        // 选中
                        selectedItems.add(index)
                        background = resources.getDrawable(R.drawable.bg_yellow, null)
                    }
                    updateChooseAllState()
                }
            }
            list.addView(textView)
        }

        // 全选按钮
        chooseAll.setOnCheckedChangeListener { _, isChecked ->
            for (i in 0 until list.childCount) {
                val textView = list.getChildAt(i) as? TextView
                if (isChecked) {
                    selectedItems.add(i)
                    textView?.background = resources.getDrawable(R.drawable.bg_yellow, null)
                } else {
                    selectedItems.clear()
                    textView?.background = resources.getDrawable(R.drawable.green_bg, null)
                }
            }
        }
    }

    private fun updateChooseAllState() {
        chooseAll.setOnCheckedChangeListener(null)
        chooseAll.isChecked = selectedItems.size == practiceItems.size && practiceItems.isNotEmpty()
        chooseAll.setOnCheckedChangeListener { _, isChecked ->
            for (i in 0 until list.childCount) {
                val textView = list.getChildAt(i) as? TextView
                if (isChecked) {
                    selectedItems.add(i)
                    textView?.background = resources.getDrawable(R.drawable.bg_yellow, null)
                } else {
                    selectedItems.clear()
                    textView?.background = resources.getDrawable(R.drawable.green_bg, null)
                }
            }
        }
    }

    // 获取选中的项目
    private fun getSelectedItems(): List<PracticeItem> {
        return selectedItems.mapNotNull { index ->
            if (index < practiceItems.size) practiceItems[index] else null
        }
    }

    fun loadPracticeItems(context: Context): List<PracticeItem> {
        val jsonString = try {
            context.assets.open("bWord.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }

        return parseJsonToPracticeItems(jsonString)
    }

    private fun parseJsonToPracticeItems(jsonString: String): List<PracticeItem> {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        val jsonMap: Map<String, Any> = gson.fromJson(jsonString, type)

        val practiceItems = mutableListOf<PracticeItem>()

        // 获取 "b" 数组，其中包含所有拼音键
        val bArray = jsonMap["b"] as? List<String> ?: return emptyList()

        // 遍历每个拼音键
        for (pinyin in bArray) {
            val words = jsonMap[pinyin] as? List<String> ?: continue
            practiceItems.add(PracticeItem(pinyin = pinyin, words = words))
        }

        return practiceItems
    }
}