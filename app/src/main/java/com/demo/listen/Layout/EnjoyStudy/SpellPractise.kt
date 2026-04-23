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
    private lateinit var btTry: Button
    private lateinit var tvTip: TextView
    private lateinit var tvScore: TextView
    private lateinit var btSeeReport: Button

    // 四种状态：not_spell_yet, spelling, assessing, spelled
    private var state: String? = "not_spell_yet"
    private lateinit var scoreList: Array<Int>  // 根据分数判断是否读过


    private lateinit var pinyinList: List<String>
    private var index: Int? = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practise)

        index = intent.getIntExtra("index", 0)
        pinyinList = intent.getStringArrayListExtra("pinyin_list") ?: listOf("None")
        scoreList = Array(pinyinList.size) {0}

        initPage()
        handleClick()

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
        btTry = findViewById<Button>(R.id.bt_try_report)
        tvTip = findViewById<TextView>(R.id.tv_spell_tip)
        tvScore = findViewById<TextView>(R.id.tv_spell_score)
        btSeeReport = findViewById<Button>(R.id.bt_spell_see_report)

        target.text = pinyinList[index?:0]
        counter.text = "${if (index != null) index!! + 1 else 0}/${pinyinList.size}"
//        player.setVideoURI(getUri())

        if (index == 0) btPre.visibility = View.INVISIBLE
        if (index == pinyinList.size-1)  btNxt.visibility = View.INVISIBLE
        tvTip.visibility = View.INVISIBLE
        tvScore.visibility = View.INVISIBLE
        btSeeReport.visibility = View.INVISIBLE
    }

    // 根据拼音获取对应的 uri
    private fun getUri(): Uri {
        // TODO: 根据 index 获取对应拼音的uri
        return Uri.EMPTY
    }

    private fun handleClick() {
        btPre.setOnClickListener {
            if (state == "spelling") return@setOnClickListener  // 说话时不能切换
            index?.let { currentIndex ->
                if (currentIndex > 0) {
                    index = currentIndex - 1
                    updateContent()
                }
            }
        }

        btNxt.setOnClickListener {
            if (state == "spelling") return@setOnClickListener
            index?.let { currentIndex ->
                if (currentIndex < pinyinList.size - 1) {
                    index = currentIndex + 1
                    updateContent()
                }
            }
        }

        btTry.setOnClickListener {
            // 四种状态：not_spell_yet, spelling, assessing, spelled
            when(state) {
                "not_spell_yet", "spelled" -> {
                    btTry.text = "说完了"
                    state = "spelling"
                    // TODO: 开启录音
                }
                "spelling" -> {
                    // TODO: 结束录音
                    btTry.text = "评估中……"
                    state = "assessing"
                    getScore()
                }
            }
        }
    }

    private fun updateContent() {
        val currentIndex = index ?: 0
        target.text = pinyinList[currentIndex]
        counter.text = "${currentIndex + 1}/${pinyinList.size}"

        // 更新视频资源
//        player.setVideoURI(getUri())
        // 重置视频播放状态
//        player.seekTo(0)
//        player.pause()

        // 更新组件可见性
        btPre.visibility = if (currentIndex == 0) View.INVISIBLE else View.VISIBLE
        btNxt.visibility = if (currentIndex == pinyinList.size - 1) View.INVISIBLE
                            else View.VISIBLE

        if (scoreList[currentIndex] == 0) {
            tvScore.visibility = View.INVISIBLE
            tvTip.visibility = View.INVISIBLE
            btTry.text = "尝试"
            state = "not_spell_yet"
        } else {
            tvScore.visibility = View.VISIBLE
            tvTip.visibility = View.VISIBLE
            btTry.text = "再次尝试"
            state = "spelled"
            tvScore.text = "${scoreList[currentIndex]}"
            if (currentIndex == pinyinList.size && !scoreList.contains(0))
                btSeeReport.visibility = View.VISIBLE   // 练习完，一直显示
        }
    }

    // pos: 请求获取分数的拼音
    private fun getScore() {
        // TODO: 传送音频给服务器，获取分数，超过10s停止
        // 更新分数 ==> 线程
        updateScore(11)    // test
    }

    // 得到分数后，更新分数列表，在当前界面显示分数
    // pos：分数对应的拼音
    // score：分数
    fun updateScore(score: Int = 1) {
        scoreList[index?:0] = score
        tvTip.visibility = View.VISIBLE
        tvTip.text = "您的得分为:"

        tvScore.visibility = View.VISIBLE
        tvScore.text = "${score}"

        state = "spelled"
        btTry.text = "再次尝试"
    }

    fun allAssessed(array: Array<Int>): Boolean {
        for (num in array) {
            if (num == 0) return false
        }
        return true
    }

}