package com.demo.listen.Layout.Assessment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentBinaryChoice.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentBinaryChoice : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var canChoose = false
    private var chooseFirst = false
    private var chooseSecond = false
    private lateinit var viewModel: ShareListenState    // 没有播放过音频不能做选择

    private lateinit var firstChoice: TextView
    private lateinit var secondChoice: TextView

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_binary_choice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstChoice = requireView().findViewById<TextView>(R.id.choice_first)
        secondChoice = requireView().findViewById<TextView>(R.id.choice_second)

        initPage()

        viewModel = ViewModelProvider(requireActivity())[ShareListenState::class.java]
        viewModel.played.observe(viewLifecycleOwner) { played ->
            canChoose = played
        }
        viewModel.options.observe(viewLifecycleOwner) { options ->
            updateOptions(options)
        }

        handleClick()
    }

    private fun initPage() {
        firstChoice.setBackgroundResource(R.drawable.bg_choice)
        secondChoice.setBackgroundResource(R.drawable.bg_choice)
    }

    private fun handleClick() {
        firstChoice.setOnClickListener {
            if (canChoose) {
                firstChoice.setBackgroundResource(R.drawable.bg_choice_selected)
                if (chooseSecond) {
                    chooseSecond = false
                    secondChoice.setBackgroundResource(R.drawable.bg_choice)
                }
                viewModel.updateChoice(firstChoice.text.toString())
            } else {
                Toast.makeText(requireContext(), "请先听音频",
                    Toast.LENGTH_SHORT).show()
            }
        }
        secondChoice.setOnClickListener {
            if (canChoose) {
                secondChoice.setBackgroundResource(R.drawable.bg_choice_selected)
                if (chooseFirst) {
                    chooseFirst = false
                    firstChoice.setBackgroundResource(R.drawable.bg_choice)
                }
                viewModel.updateChoice(secondChoice.text.toString())
            } else {
                Toast.makeText(requireContext(), "请先听音频",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateOptions(options: List<String>) {
        initPage()
        if (options.size >= 2) {
            firstChoice.text = options[0]
            secondChoice.text = options[1]
        } else {
            // 处理异常情况
            firstChoice.text = "Error"
            secondChoice.text = "Error"
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentBinaryChoice.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentBinaryChoice().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}