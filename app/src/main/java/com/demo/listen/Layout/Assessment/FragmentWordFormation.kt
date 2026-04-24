package com.demo.listen.Layout.Assessment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.Adapter.WordFormationAdapter
import com.demo.listen.Layout.DataType.Word
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentWordFormation.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentWordFormation : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var wordFormationRet: TextView
    private lateinit var wordList: RecyclerView
    private lateinit var wordAdapter: WordFormationAdapter
    private val wordsList = mutableListOf<Word>()
    private var currentSelectedWords = mutableListOf<Word>()

    private var canChoose = false
    private lateinit var viewModel: ShareListenState    // 没有播放过音频不能做选择

    private var currentResult: String = ""
        set(value) {
            field = value
            updateResultDisplay()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_word_formation,
            container, false)

        wordFormationRet = view.findViewById(R.id.word_formation_ret)
        wordList = view.findViewById(R.id.word_list)
        wordFormationRet.text = "点击下方文字进行组词"

        viewModel = ViewModelProvider(requireActivity())[ShareListenState::class.java]
        viewModel.played.observe(viewLifecycleOwner) { played ->
            canChoose = played
        }
        viewModel.options.observe(viewLifecycleOwner) { options ->
            loadWords(options)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadWords(words)
    }

    private fun setupRecyclerView() {
        wordAdapter = WordFormationAdapter(emptyList()) { word, position ->
            handleWordClick(word, position)
        }

        wordList.apply {
            layoutManager = GridLayoutManager(requireContext(), 6)
            adapter = wordAdapter
        }
    }

    private fun clearAll() {
        currentSelectedWords.clear()
        wordsList.forEach { it.isSelected = false }
        currentResult = ""
        wordAdapter.updateWords(wordsList)
    }

    val words = listOf( // test
        "苹", "果", "水", "果", "桃", "子"
    )

    private fun loadWords(options: List<String>) {
        clearAll()
        wordsList.clear()
        wordsList.addAll(options.map { Word(it, false) })
        wordAdapter.updateWords(wordsList)
    }

    private fun handleWordClick(word: Word, position: Int) {
        if (!canChoose) {
            Toast.makeText(requireContext(), "请先听音频",
                Toast.LENGTH_SHORT).show()
            return
        }
        word.isSelected = !word.isSelected

        if (word.isSelected) {
            currentSelectedWords.add(word)
        } else {
            currentSelectedWords.remove(word)
        }

        buildResultString()
        wordAdapter.updateWordAtPosition(position, word)
    }

    private fun buildResultString() {
        val resultBuilder = StringBuilder()
        currentSelectedWords.forEach { selectedWord ->
            resultBuilder.append(selectedWord.text)
        }

        currentResult = resultBuilder.toString()
        viewModel.updateChoice(resultBuilder.toString())
    }

    private fun updateResultDisplay() {
        val spannableString = SpannableString(currentResult)

        for (i in 0 until currentResult.length) {
            spannableString.setSpan(
                UnderlineSpan(),
                i,
                i + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        wordFormationRet.text = if (currentResult.isEmpty()) {
            "点击下方文字进行组词"
        } else {
            spannableString
        }
    }

    fun getCurrentResult(): String = currentResult

    fun getSelectedWords(): List<Word> = currentSelectedWords.toList()

    fun clearSelection() {
        currentSelectedWords.clear()
        wordsList.forEach { it.isSelected = false }
        currentResult = ""
        wordAdapter.updateWords(wordsList)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentWordFormation.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentWordFormation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}