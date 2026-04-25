package com.demo.listen.Layout.Assessment

import android.os.Bundle
import android.widget.CheckBox
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
import org.json.JSONObject

class PracticeList : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var chooseAll: CheckBox
    private lateinit var list: RecyclerView
    private lateinit var start: TextView

    private var syllable: Char = '-'
    private var mode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practice_list)

        var pinyin = intent.getStringExtra("pinyin") ?: "<None>"
        mode = intent.getStringExtra("mode") ?: "<None>"
        syllable = pinyin[0]
        Toast.makeText(this, "$syllable",
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
        list = findViewById<RecyclerView>(R.id.practice_list_list)
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
    }
}