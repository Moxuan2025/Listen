package com.demo.listen.Layout.EnjoyStudy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.R
import com.demo.listen.net.OralEvaluationHelper
import com.demo.listen.net.PinyinHanziMap
import com.demo.listen.net.TencentSpeechHelper
import com.demo.listen.net.PinyinUtils
import com.tencent.cloud.soe.TAIOralController

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FragmentPracticeContent : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var ivView: ImageView
    private lateinit var tvPinyinCenter: TextView
    private lateinit var tvPinYin: TextView
    private lateinit var tvWord: TextView
    private lateinit var playExample: ImageView
    private lateinit var score: TextView
    private lateinit var playRecord: ImageView
    private lateinit var spellAction: TextView
    private lateinit var record: ImageButton
    private lateinit var recordTip: TextView
    private lateinit var preTone: TextView
    private lateinit var nxtTone: TextView

    private lateinit var viewModel: SharePracticeData
    private var action = listOf<String>("")
    private var curIndex: Int = 0

    private var syllable = ""
    private var scoreList: MutableList<Int>? = mutableListOf()

    private var isExamplePlaying = false

    // ========== 口语评测相关变量 ==========
    private var oralController: TAIOralController? = null
    private var isRecording = false
    private var ignoreResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            syllable = bundle.getString("Syllable") ?: "<None>"
            Toast.makeText(requireContext(), syllable, Toast.LENGTH_SHORT).show()
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
        return inflater.inflate(R.layout.fragment_practice_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapWidget()
        initPage()
        handleClick()
    }

    private fun mapWidget() {
        ivView = requireView().findViewById(R.id.example_view)
        tvPinyinCenter = requireView().findViewById(R.id.practice_content_label)
        tvPinYin = requireView().findViewById(R.id.tv_pinyin)
        tvWord = requireView().findViewById(R.id.tv_content)
        spellAction = requireView().findViewById(R.id.spell_action)
        playExample = requireView().findViewById(R.id.example_sound_play)
        playRecord = requireView().findViewById(R.id.record_sound_play)
        record = requireView().findViewById(R.id.practise_sound_record)
        score = requireView().findViewById(R.id.practice_score)
        recordTip = requireView().findViewById(R.id.practise_sound_record_tip)
        preTone = requireView().findViewById(R.id.pc_pre)
        nxtTone = requireView().findViewById(R.id.pc_nxt)
    }

    private var next: String = "word"
    private var target: List<String> = listOf()
    private var type = "pinyin"
    private var contentList = listOf<WordPinYin>()
    private var totalItem = 0

    private fun initPage() {
        viewModel = ViewModelProvider(requireActivity())[SharePracticeData::class.java]

        tvPinyinCenter.visibility = View.INVISIBLE
        ivView.visibility = View.INVISIBLE
        tvWord.visibility = View.INVISIBLE
        tvPinYin.visibility = View.INVISIBLE

        viewModel.action.observe(viewLifecycleOwner) { actions -> action = actions }
        viewModel.nextPage.observe(viewLifecycleOwner) { nextPage -> next = nextPage }
        viewModel.target.observe(viewLifecycleOwner) { targets ->
            if (targets.isNotEmpty()) {
                target = targets
                totalItem = target.size
                scoreList = MutableList(totalItem) { -1 }
            }
        }
        viewModel.wordPinYin.observe(viewLifecycleOwner) { wpy ->
            if (wpy.isNotEmpty()) {
                contentList = wpy
                totalItem = wpy.size
                scoreList = MutableList(totalItem) { -1 }
            }
        }
        viewModel.type.observe(viewLifecycleOwner) { vtype ->
            type = vtype
            ivView.visibility = View.VISIBLE
            when (type) {
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
        val sc = scoreList?.get(curIndex)
        score.text = "--"

        when (type) {
            "pinyin" -> {
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
        if (sc != -1) {
            playRecord.setImageResource(R.drawable.ic_play_sound)
            score.text = sc.toString()
        }

        preTone.text = "上一个"
        preTone.visibility = View.VISIBLE
        nxtTone.text = "下一个"
        if (curIndex == 0) preTone.visibility = View.INVISIBLE
        if (curIndex == totalItem - 1) {
            when (next) {
                "word" -> nxtTone.text = "进阶!"
                "report" -> nxtTone.text = "查看报告"
            }
        }
    }

    private fun handleClick() {
        // ============ 播放示例音频 ============
        playExample.setOnClickListener {
            if (isExamplePlaying) {
                Toast.makeText(requireContext(), "正在播放，请稍后", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rawText = when (type) {
                "pinyin" -> target.getOrElse(curIndex) { "" }
                "word", "phrase", "sentence" -> contentList.getOrNull(curIndex)?.word ?: ""
                else -> ""
            }

            if (rawText.isBlank()) {
                Toast.makeText(requireContext(), "没有可朗读的内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val textToSpeak = if (type == "pinyin") {
                val numberPinyin = PinyinUtils.convertToneMarkToNumber(rawText)
                PinyinUtils.buildPinyinSsml(numberPinyin)
            } else {
                rawText
            }

            isExamplePlaying = true
            TencentSpeechHelper.synthesisAndPlay(
                text = textToSpeak,
                context = requireContext(),
                onComplete = { isExamplePlaying = false }
            )
        }

        // 原有导航
        preTone.setOnClickListener {
            curIndex -= 1
            if (curIndex < 0) curIndex = 0
            viewModel.changeIndex(curIndex)
        }
        nxtTone.setOnClickListener {
            curIndex += 1
            if (curIndex == totalItem) {
                when (next) {
                    "word" -> startActivity(Intent(requireActivity(), PracticeList::class.java).apply {
                        putExtra("Syllable", syllable)
                        putExtra("mode", "word")
                    })
                    "report" -> startActivity(Intent(requireActivity(), PracticeFeedback::class.java).apply {})
                }
                requireActivity().finish()
            } else {
                viewModel.changeIndex(curIndex)
            }
        }

        // ============ 录音按钮：统一走真实评测 ============
        record.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isExamplePlaying || isRecording) return@setOnTouchListener true

                    val rawRefText = when (type) {
                        "pinyin" -> target.getOrElse(curIndex) { "" }
                        "word", "phrase", "sentence" -> contentList.getOrNull(curIndex)?.word ?: ""
                        else -> ""
                    }

                    // 如果是拼音模式，尝试转换为汉字；转换失败则提示并终止本次录音
                    val refText: String
                    if (type == "pinyin" && rawRefText.isNotBlank()) {
                        val hanzi = PinyinHanziMap.getHanzi(rawRefText)
                        if (hanzi == null) {
                            Toast.makeText(requireContext(), "该拼音暂不支持评测", Toast.LENGTH_SHORT).show()
                            return@setOnTouchListener true
                        }
                        refText = hanzi
                    } else {
                        refText = rawRefText
                    }

                    if (refText.isBlank()) {
                        Toast.makeText(requireContext(), "没有参考文本", Toast.LENGTH_SHORT).show()
                        return@setOnTouchListener true
                    }

                    android.util.Log.d("OralEval", "开始评测，参考文本: '$refText', 类型: $type")

                    record.setBackgroundResource(R.drawable.ic_record)
                    recordTip.text = "正在聆听..."
                    record.tag = System.currentTimeMillis()
                    isRecording = true
                    ignoreResult = false

                    // 所有类型均启动腾讯云口语评测
                    oralController = OralEvaluationHelper.startManualEvaluation(
                        requireContext(), refText
                    ) { resultStr ->
                        if (ignoreResult) { ignoreResult = false; return@startManualEvaluation }
                        val parsedScore = resultStr.toDoubleOrNull()
                        if (parsedScore != null) {
                            val intScore = parsedScore.toInt()
                            scoreList?.set(curIndex, intScore)
                            score.text = intScore.toString()
                            playRecord.setImageResource(R.drawable.ic_play_sound)
                        } else {
                            score.text = "评分失败"
                            Toast.makeText(requireContext(), "评测失败: $resultStr", Toast.LENGTH_LONG).show()
                        }
                        resetRecordButton()
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isRecording) return@setOnTouchListener true
                    val downTime = record.tag as? Long
                    val pressDuration = if (downTime != null) System.currentTimeMillis() - downTime else 0L

                    if (pressDuration < 800) {
                        // 短按：取消本次评测，不记录分数
                        ignoreResult = true
                        OralEvaluationHelper.stopManualEvaluation(oralController)
                        resetRecordButton()
                        Toast.makeText(requireContext(), "没听到呢，能再试一下吗", Toast.LENGTH_SHORT).show()
                    } else {
                        // 长按：停止录音，等待回调自动更新分数
                        OralEvaluationHelper.stopManualEvaluation(oralController)
                        // 按钮保持“聆听中”状态，由回调中的 resetRecordButton 恢复
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (isRecording) {
                        ignoreResult = true
                        OralEvaluationHelper.stopManualEvaluation(oralController)
                        resetRecordButton()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun resetRecordButton() {
        record.setBackgroundResource(R.drawable.ic_record_gray)
        recordTip.text = "按住说话"
        isRecording = false
        oralController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isRecording && oralController != null) {
            OralEvaluationHelper.stopManualEvaluation(oralController)
            oralController = null
            isRecording = false
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