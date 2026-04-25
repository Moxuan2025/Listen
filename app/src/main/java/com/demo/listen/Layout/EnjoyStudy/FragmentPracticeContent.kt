package com.demo.listen.Layout.EnjoyStudy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.Layout.EnjoyStudy.PracticeList
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPracticeContent.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPracticeContent : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var ivView: ImageView                // 内容的讲解示例图片
    private lateinit var tvPinyinCenter: TextView         // 学习内容, type == "pinyin"
    private lateinit var tvPinYin: TextView               // 学习内容, type != "word"
    private lateinit var tvWord: TextView                 // 学习内容, type != "word"
    private lateinit var playExample: ImageView
    private lateinit var score: TextView
    private lateinit var playRecord: ImageView      // 播放录音
    private lateinit var spellAction: TextView
    private lateinit var record: ImageButton        // 录音
    private lateinit var recordTip: TextView
    private lateinit var preTone: TextView
    private lateinit var nxtTone: TextView


    private lateinit var viewModel: SharePracticeData
    private var action = listOf<String>("")
    private var curIndex: Int = 0

    private var syllable = ""
    private var scoreList: MutableList<Int> ?= mutableListOf()        // 学习/练习 数据记录
    // TODO: 一个变量记录录音数据，用于播放和上传

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            syllable = bundle.getString("Syllable") ?: "<None>"
            Toast.makeText(requireContext(), syllable,
                Toast.LENGTH_SHORT).show()
        }

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_practice_content,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapWidget()
        initPage()
        handleClick()
    }

    private fun mapWidget() {
        ivView = requireView().findViewById<ImageView>(R.id.example_view)

        tvPinyinCenter = requireView().findViewById<TextView>(R.id.practice_content_label)
        tvPinYin = requireView().findViewById<TextView>(R.id.tv_pinyin)
        tvWord = requireView().findViewById<TextView>(R.id.tv_content)
        spellAction = requireView().findViewById<TextView>(R.id.spell_action)

        playExample = requireView().findViewById<ImageView>(R.id.example_sound_play)
        playRecord = requireView().findViewById<ImageView>(R.id.record_sound_play)
        record = requireView().findViewById<ImageButton>(R.id.practise_sound_record)

        score = requireView().findViewById<TextView>(R.id.practice_score)
        recordTip = requireView().findViewById<TextView>(R.id.practise_sound_record_tip)
        preTone = requireView().findViewById<TextView>(R.id.pc_pre)
        nxtTone = requireView().findViewById<TextView>(R.id.pc_nxt)
    }

    private var next: String = "word"
    private var target: List<String> = listOf()
    private var showView: Boolean = false

    private var type = "pinyin"     // 传入数据的类型：拼音 pinyin、词汇 word、词组 phrase、句子 sentence
    private var contentList = listOf<WordPinYin>()  // 数据

    private var totalItem = 0

    private fun initPage() {
        viewModel = ViewModelProvider(requireActivity())[SharePracticeData::class.java]

        tvPinyinCenter.visibility = View.INVISIBLE
        ivView.visibility = View.INVISIBLE
        tvWord.visibility = View.INVISIBLE
        tvPinYin.visibility = View.INVISIBLE

        viewModel.action.observe(viewLifecycleOwner) { actions ->
            action = actions
        }
        viewModel.nextPage.observe(viewLifecycleOwner) { nextPage ->
            next = nextPage
        }
        viewModel.target.observe(viewLifecycleOwner) {targets ->
            if (targets.isNotEmpty()) {
                target = targets
                totalItem = target.size
                scoreList = MutableList(totalItem) {-1}
            }
        }
        viewModel.wordPinYin.observe(viewLifecycleOwner) {wpy ->
            if (wpy.isNotEmpty()) {
                contentList = wpy
                totalItem = wpy.size
                scoreList = MutableList(totalItem) {-1}
            }
        }
        viewModel.type.observe(viewLifecycleOwner) {vtype ->
            type = vtype
            showView = true     // 可视
            ivView.visibility = View.VISIBLE
            when(type) {
                "pinyin" -> tvPinyinCenter.visibility = View.VISIBLE
                "word", "phrase", "sentence" -> {
                    tvWord.visibility = View.VISIBLE
                    tvPinYin.visibility = View.VISIBLE
                }
            }
        }
        viewModel.index.observe(viewLifecycleOwner) { index ->
            curIndex = index
            changePage()
        }
    }

    private fun changePage() {
        var sc = scoreList?.get(curIndex)
        score.text = "--"

        when(type) {
            "pinyin" -> {  // TODO: 修改本条代码
                tvPinyinCenter.text = target[curIndex]
                spellAction.text = "发声动作:\n" + action[curIndex]
            }
            "word", "phrase", "sentence" -> {
                tvWord.text = contentList[curIndex].word
                tvPinYin.text = contentList[curIndex].pinyin
                spellAction.text = "发声动作:\n" + contentList[curIndex].action
            }
        }

        playRecord.setImageResource(R.drawable.ic_play_sound_gray)
        if (sc != -1){
            playRecord.setImageResource(R.drawable.ic_play_sound)
            score.text = sc.toString()
        }

        preTone.text = "上一个"
        preTone.visibility = View.VISIBLE
        nxtTone.text = "下一个"
        if (curIndex == 0) preTone.visibility = View.INVISIBLE
        if (curIndex == totalItem-1) {
            when(next) {
                "word" -> nxtTone.text = "进阶!"
                "report" -> nxtTone.text = "查看报告"
            }
        }
    }

    private fun handleClick() {
        preTone.setOnClickListener {
            curIndex -= 1
            if (curIndex < 0) curIndex = 0
            viewModel.changeIndex(curIndex)     // 通知 activity 改变
        }
        nxtTone.setOnClickListener {
            curIndex += 1
            if (curIndex == totalItem) {              // 什么时候到达末尾
                when(next) {
                    "word" -> {             // 去学习词汇
                        startActivity(Intent(requireActivity(),
                            PracticeList::class.java).apply {
                            putExtra("Syllable", syllable)
                            putExtra("mode", "word")
                        })
                    }
                    "report"  -> {
                        // TODO: 跳转到正确的报告页面
                        startActivity(Intent(requireActivity(),
                            PracticeFeedback::class.java).apply {
                        })
                    }
                }
                requireActivity().finish()
            } else
                viewModel.changeIndex(curIndex)
        }

        record.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    record.setBackgroundResource(R.drawable.ic_record)
                    recordTip.text = "聆听中"
                    record.tag = System.currentTimeMillis()
                    // TODO: 开始录音
                }
                MotionEvent.ACTION_UP -> {
                    record.setBackgroundResource(R.drawable.ic_record_gray)
                    recordTip.text = "按住说话"
                    // TODO: 结束录音
                    val downTime = record.tag as? Long
                    if (downTime != null) {
                        val pressDuration = System.currentTimeMillis() - downTime
                        if (pressDuration < 800) {
                            Toast.makeText(requireContext(), "没听到呢，能再试一下吗",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            // TODO: 评估分数
                            scoreList?.set(curIndex, 90)
                            score.text = scoreList?.get(curIndex).toString()
                            playRecord.setImageResource(R.drawable.ic_play_sound)
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    record.setBackgroundResource(R.drawable.ic_record_gray)
                }
            }
            true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentPracticeContent().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}