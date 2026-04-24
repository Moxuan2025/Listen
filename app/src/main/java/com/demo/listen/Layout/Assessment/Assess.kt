package com.demo.listen.Layout.Assessment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.Layout.DataType.BinaryChoice
import com.demo.listen.R
import java.io.File

class Assess : AppCompatActivity() {

    private lateinit var btSure: Button
    private lateinit var problemType: TextView
    private lateinit var progressCounter: TextView
    private lateinit var progress: ProgressBar
    private lateinit var answer: TextView
    private lateinit var tip: TextView
    private lateinit var problemTxt: TextView
    private lateinit var playSound: LinearLayout
    private lateinit var playSoundIB: ImageButton
    private lateinit var problemArea: FrameLayout
    private lateinit var fRecord: LinearLayout
    private lateinit var record: ImageView

    private val fBinaryChoice = FragmentBinaryChoice()


    private var problemTotalNum = 15
    private var index = 0
    private var state: String = "not_answer_yet"    // 两种状态：answered, not_answer_yet
    private lateinit var viewModel: ShareListenState    // 没有播放过音频不能做选择

    private var level = 4           // 残疾程度
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assess)

        level = intent.getIntExtra("level", 4)
        mapWidget()
        initPage()
        handleClick()
        btSure.visibility = View.INVISIBLE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        viewModel = ViewModelProvider(this@Assess)[ShareListenState::class.java]

        btSure = findViewById<Button>(R.id.assess_sure)
        problemType = findViewById<TextView>(R.id.assess_problem_type)
        progressCounter = findViewById<TextView>(R.id.assess_problem_counter)
        progress = findViewById<ProgressBar>(R.id.progress_bar)
        answer = findViewById<TextView>(R.id.assess_answer)
        tip = findViewById<TextView>(R.id.assess_tip)
        problemTxt = findViewById<TextView>(R.id.assess_problem_txt)
        playSound = findViewById<LinearLayout>(R.id.assess_play_sound)
        playSoundIB = findViewById<ImageButton>(R.id.assess_play_sound_ib)
        problemArea = findViewById<FrameLayout>(R.id.assess_problem_area)
        fRecord = findViewById<LinearLayout>(R.id.frame_record)
        record = findViewById<ImageView>(R.id.assess_sound_record)
    }

    private var isInit = true
    @SuppressLint("ResourceAsColor")
    private fun initPage() {
        loadFragment(fBinaryChoice)
        problemTxt.text = ""
        viewModel.choice.observe(this) { choice ->
            if (isInit) {
                isInit = false
                return@observe
            }
            state = "answered"
            answer.text = sigleSelection[index].getAnswer()
            btSure.visibility = View.VISIBLE
        }
        viewModel.updateOptions(sigleSelection[index].optionList())
    }

    private fun handleClick() {
        btSure.setOnClickListener {
            goNext()
        }
        playSound.setOnClickListener {
            getPlaySound()
        }
        playSoundIB.setOnClickListener {
            getPlaySound()
        }

        record.setOnTouchListener { _, event ->
            // TODO: 记录录音
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopRecording()
                    true
                }
                else -> false
            }
        }
    }

    private fun getPlaySound() {
        tip.text = "播放中"
        // TODO: get and play sound
        tip.text = "重新播放"

        viewModel.updatePlayState(true)    // 可以做选择了
    }

    // test
    val sigleSelection = listOf(
        BinaryChoice("wu", "hu"),       //读音
        BinaryChoice("gu", "ga"),
        BinaryChoice("cong", "zong"),
        BinaryChoice("ca", "che"),      // 词语
        BinaryChoice("wu", "hu"),
        BinaryChoice("gu", "ga"),

        BinaryChoice("cong", "zong"),   // 完成句子
        BinaryChoice("wu", "hu"),
        BinaryChoice("gu", "ga"),

        BinaryChoice("wu", "hu"),       // 跟读
        BinaryChoice("gu", "ga"),
        BinaryChoice("cong", "zong"),

        BinaryChoice("wu", "hu"),       // 组词. 多选
        BinaryChoice("gu", "ga"),
        BinaryChoice("cong", "zong"),
    )

    private fun goNext() {
        var showIndex = index+1

        // 两种状态：answered, not_answer_yet
        if (state == "not_answer_yet") {    // 还未回答问题，不允许下一题
            Toast.makeText(this@Assess, "您还没有回答问题哦!",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (showIndex == problemTotalNum && state == "answered") {  // 完成所有评估
            startActivity(Intent(this@Assess,
                AssessReport::class.java).apply {
//                putExtra("choice", ArrayList(choice))
//                putExtra("target", ArrayList(target))
            })
            finish()
        }

        state = "not_answer_yet"                   // 更新状态
        viewModel.updatePlayState(false)    // 不能做选择

        progressCounter.text = "${showIndex}/${problemTotalNum}"
        ObjectAnimator.ofInt(progress, "progress",
            index, showIndex).apply {
            duration = 1000
            start()
        }
        answer.text = "?"
        btSure.visibility = View.INVISIBLE

        when (showIndex) {
            7 -> {
                problemType.text = "完成句子"
            }
            10 -> {
                problemTxt.text = ""
                removeCurrentFragment()
                fRecord.visibility = View.VISIBLE
                problemType.text = "跟读"
            }
            13 -> {
                fRecord.visibility = View.GONE
                // TODO: load new fragment
                problemType.text = "选字组词"
            }
        }

        when (showIndex) {
            1,2,3 -> {
                tip.text = "听录音，选择正确的读音"
                viewModel.updateOptions(sigleSelection[showIndex].optionList())
            }
            4,5,6 -> {
                tip.text = "听录音，选择正确的词语"
                viewModel.updateOptions(sigleSelection[showIndex].optionList())
            }
            7,8,9 -> {
                tip.text = "听录音，选择正确的词语"
                viewModel.updateOptions(sigleSelection[showIndex].optionList())
            }
            10, 11, 12 -> {
                tip.text = "听录音，读出正确的读音"
                viewModel.updateOptions(sigleSelection[showIndex].optionList())
                // TODO: 录音
            }
            else -> {
                tip.text = "听录音，选字组词"
                viewModel.updateOptions(sigleSelection[showIndex].optionList())
            }
        }
        index++
    }

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFile: File? = null
    private var startTime = 0L  // 记录开始时间
    private val MIN_RECORD_TIME = 3000L  // 最小录音时长 3秒（毫秒）

    private fun startRecording() {
        if (isRecording) return

        try {
            record.setImageResource(R.drawable.ic_record)   // 需要添加录音中的图标
            startTime = System.currentTimeMillis()          // 记录开始时间
            // 创建音频文件
            audioFile = File(externalCacheDir,
                "audio_${System.currentTimeMillis()}.3gp")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)

                prepare()
                start()
            }

            isRecording = true
            Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val duration = System.currentTimeMillis() - startTime   // 计算录音时长

            if (duration < MIN_RECORD_TIME) {
                // 录音时间过短，删除文件
                audioFile?.delete()
                audioFile = null
                Toast.makeText(this,
                    "录音时间过短（至少${MIN_RECORD_TIME/1000}秒），已取消",
                    Toast.LENGTH_SHORT).show()
            } else {
                // 录音有效，处理文件
                Toast.makeText(this,
                    "录音完成，时长：${duration/1000}秒",
                    Toast.LENGTH_SHORT).show()
                audioFile?.let { file ->
                    handleRecordedAudio(file)
                }
            }
            record.setImageResource(R.drawable.ic_record_gray)  // 恢复图标样式
            state = "answered"
            btSure.visibility = View.VISIBLE

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "录音结束异常: ${e.message}",
                Toast.LENGTH_SHORT).show()
            // 异常时也删除文件
            audioFile?.delete()
        }
    }

    private fun handleRecordedAudio(file: File) {
        // TODO: 发送录音，评估
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.assess_problem_area, fragment)
            .commit()
    }

    private fun removeCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.assess_problem_area)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }
}