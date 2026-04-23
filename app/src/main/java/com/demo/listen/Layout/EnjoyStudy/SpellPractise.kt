package com.demo.listen.Layout.EnjoyStudy

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R

class SpellPractise : AppCompatActivity() {

    private lateinit var target: TextView
    private lateinit var player: VideoView
    private lateinit var counter: TextView
    private lateinit var btPre: Button
    private lateinit var btNxt: Button
    private lateinit var btTryReport: Button

    private lateinit var pinyinList: List<String>
    private var index: Int? = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practise)

        index = intent.getIntExtra("index", 0)
        pinyinList = intent.getStringArrayListExtra("pinyin_list") ?: listOf("None")

        initPage()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initPage() {
        target = findViewById<TextView>(R.id.tv_spell_practise_target)
        player = findViewById<VideoView>(R.id.pv_example_view)
        counter = findViewById<TextView>(R.id.tv_spell_practise_counter)
        btPre = findViewById<Button>(R.id.bt_spell_pre)
        btNxt = findViewById<Button>(R.id.bt_spell_nxt)
        btTryReport = findViewById<Button>(R.id.bt_try_report)

        target.text = pinyinList[index?:0]
        counter.text = "${if (index != null) index!! + 1 else 0}/${pinyinList.size}"
//        player.setVideoURI(getUri())

        if (index == 0) btPre.visibility = View.INVISIBLE
        if (index == pinyinList.size-1)  btNxt.visibility = View.INVISIBLE
    }

    // 根据拼音获取对应的 url
    private fun getUri(): Uri {
        // TODO
        return Uri.EMPTY
    }

    private fun handleClick() {
        btPre.setOnClickListener {
            index?.let { currentIndex ->
                if (currentIndex > 0) {
                    index = currentIndex - 1
                    updateContent()
                }
            }
        }

        btNxt.setOnClickListener {
            index?.let { currentIndex ->
                if (currentIndex < pinyinList.size - 1) {
                    index = currentIndex + 1
                    updateContent()
                }
            }
        }

        btTryReport.setOnClickListener {
        }
    }

    private fun updateContent() {
        val currentIndex = index ?: 0
        target.text = pinyinList[currentIndex]
        counter.text = "${currentIndex + 1}/${pinyinList.size}"

        // 更新视频资源
        player.setVideoURI(getUri())

        // 更新按钮可见性
        btPre.visibility = if (currentIndex == 0) View.INVISIBLE else View.VISIBLE
        btNxt.visibility = if (currentIndex == pinyinList.size - 1) View.INVISIBLE else View.VISIBLE

        // 可选：重置视频播放状态
        player.seekTo(0)
        player.pause()
    }

}