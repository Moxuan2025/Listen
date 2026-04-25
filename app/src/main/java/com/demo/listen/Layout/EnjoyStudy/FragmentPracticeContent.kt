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


    private lateinit var ivView: ImageView          // 内容的讲解示例图片
    private lateinit var content: TextView         // 学习内容
    private lateinit var playExample: ImageView
    private lateinit var score: TextView
    private lateinit var playRecord: ImageView      // 播放录音
    private lateinit var spellAction: TextView
    private lateinit var record: ImageButton        // 录音
    private lateinit var recordTip: TextView
    private lateinit var preTone: TextView
    private lateinit var nxtTone: TextView


    private lateinit var viewModel: SharePracticeData
    private var action = listOf<String>("", "", "", "")
    private var curIndex: Int = 0

    private var syllable = ""
    private var scoreList = mutableListOf(0, 0, 0, 0)  // 记录为字符串，方便替换
    // TODO: 一个变量记录录音数据

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
        content = requireView().findViewById<TextView>(R.id.practice_content_label)
        playExample = requireView().findViewById<ImageView>(R.id.example_sound_play)
        score = requireView().findViewById<TextView>(R.id.practice_score)
        playRecord = requireView().findViewById<ImageView>(R.id.record_sound_play)
        spellAction = requireView().findViewById<TextView>(R.id.spell_action)
        record = requireView().findViewById<ImageButton>(R.id.practise_sound_record)
        recordTip = requireView().findViewById<TextView>(R.id.practise_sound_record_tip)
        preTone = requireView().findViewById<TextView>(R.id.pc_pre)
        nxtTone = requireView().findViewById<TextView>(R.id.pc_nxt)
    }

    private var next: String = "<None>"
    private var target: List<String> = listOf()
    private var showView: Boolean = false
    private fun initPage() {
        viewModel = ViewModelProvider(requireActivity())[SharePracticeData::class.java]

        content.visibility = View.INVISIBLE
        ivView.visibility = View.INVISIBLE

        viewModel.action.observe(viewLifecycleOwner) { actions ->
            action = actions
        }
        viewModel.index.observe(viewLifecycleOwner) { index ->
            curIndex = index
            changePage()
        }
        viewModel.nextPage.observe(viewLifecycleOwner) { nextPage ->
            next = nextPage
        }
        viewModel.target.observe(viewLifecycleOwner) {targets ->
            if (targets.isNotEmpty()) {
                target = targets
                showView = true
                content.visibility = View.VISIBLE
                ivView.visibility = View.VISIBLE
                content.text = target[curIndex]
            }
        }
    }

    private fun changePage() {
        spellAction.text = "发声动作:\n" + action[curIndex]
        score.text = scoreList[curIndex].toString()

        if (showView) content.text = target[curIndex]

        playRecord.setImageResource(R.drawable.ic_play_sound_gray)
        if (scoreList[curIndex] != 0)
            playRecord.setImageResource(R.drawable.ic_play_sound)

        preTone.text = "上一个"
        preTone.visibility = View.VISIBLE
        nxtTone.text = "下一个"
        if (curIndex == 0) preTone.visibility = View.INVISIBLE
        if (curIndex == action.size-1) nxtTone.text = "进阶!"
    }

    private fun handleClick() {
        preTone.setOnClickListener {
            curIndex -= 1
            if (curIndex < 0) curIndex = 0
            viewModel.changeIndex(curIndex)     // 通知 activity 改变
        }
        nxtTone.setOnClickListener {
            curIndex += 1
            if (curIndex == action.size) {              // 什么时候到达末尾
                if (next == "word") {                   // 去学习词汇
                    startActivity(Intent(requireActivity(),
                        PracticeList::class.java).apply {
                        putExtra("Syllable", syllable)
                        putExtra("mode", "word")        // 词汇练习
                    })
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
                            scoreList[curIndex] = 90
                            score.text = scoreList[curIndex].toString()
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