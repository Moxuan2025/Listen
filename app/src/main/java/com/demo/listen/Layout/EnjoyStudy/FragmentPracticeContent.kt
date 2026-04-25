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
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.R
import com.demo.listen.net.TencentSpeechHelper
import com.demo.listen.net.PinyinUtils

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
            // ============ 播放示例音频（SSML方式朗读拼音） ============
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

                // ✅ 改用你已验证的 PinyinUtils 处理拼音 → SSML
                val textToSpeak = if (type == "pinyin") {
                    // 将带声调符号的拼音转成数字声调，再包装为 SSML
                    val numberPinyin = PinyinUtils.convertToneMarkToNumber(rawText)
                    PinyinUtils.buildPinyinSsml(numberPinyin)
                } else {
                    rawText   // 普通文字直接朗读
                }

                isExamplePlaying = true
                TencentSpeechHelper.synthesisAndPlay(
                    text = textToSpeak,
                    context = requireContext(),
                    onComplete = {
                        isExamplePlaying = false
                    }
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
                    "word" -> {
                        startActivity(Intent(requireActivity(), PracticeList::class.java).apply {
                            putExtra("Syllable", syllable)
                            putExtra("mode", "word")
                        })
                    }
                    "report" -> {
                        startActivity(Intent(requireActivity(), PracticeFeedback::class.java).apply {})
                    }
                }
                requireActivity().finish()
            } else {
                viewModel.changeIndex(curIndex)
            }
        }

        // 原有录音逻辑
        record.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    record.setBackgroundResource(R.drawable.ic_record)
                    recordTip.text = "聆听中"
                    record.tag = System.currentTimeMillis()
                }
                MotionEvent.ACTION_UP -> {
                    record.setBackgroundResource(R.drawable.ic_record_gray)
                    recordTip.text = "按住说话"
                    val downTime = record.tag as? Long
                    if (downTime != null) {
                        val pressDuration = System.currentTimeMillis() - downTime
                        if (pressDuration < 800) {
                            Toast.makeText(
                                requireContext(), "没听到呢，能再试一下吗",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
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

    // =================== SSML 辅助方法 ===================
    private fun convertToneMarkToNumber(tonedPinyin: String): String {
        if (tonedPinyin.isEmpty()) return ""

        val toneMap = mapOf(
            'ā' to "a1", 'á' to "a2", 'ǎ' to "a3", 'à' to "a4",
            'ō' to "o1", 'ó' to "o2", 'ǒ' to "o3", 'ò' to "o4",
            'ē' to "e1", 'é' to "e2", 'ě' to "e3", 'è' to "e4",
            'ī' to "i1", 'í' to "i2", 'ǐ' to "i3", 'ì' to "i4",
            'ū' to "u1", 'ú' to "u2", 'ǔ' to "u3", 'ù' to "u4",
            'ǖ' to "v1", 'ǘ' to "v2", 'ǚ' to "v3", 'ǜ' to "v4"
        )

        val result = StringBuilder()
        var toneNumber: Char? = null

        for (ch in tonedPinyin) {
            val mapped = toneMap[ch]
            if (mapped != null) {
                result.append(mapped[0])
                toneNumber = mapped[1]
            } else {
                result.append(ch)
            }
        }

        if (toneNumber != null) {
            result.append(toneNumber)
        }

        return result.toString().lowercase()
    }

    private fun buildPinyinSSML(tonedPinyin: String): String {
        var digitalTone = convertToneMarkToNumber(tonedPinyin)

        // 无声调且含元音时补第一声
        if (!digitalTone.last().isDigit() && containsVowel(digitalTone)) {
            digitalTone += "1"
        }

        return "<speak><phoneme alphabet=\"py\" ph=\"$digitalTone\">$tonedPinyin</phoneme></speak>"
    }

    private fun containsVowel(s: String): Boolean {
        return s.any { it in "aoeiuv" }
    }
    // ===============================================================

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