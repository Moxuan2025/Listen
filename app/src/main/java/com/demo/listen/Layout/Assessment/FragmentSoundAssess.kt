package com.demo.listen.Layout.Assessment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSoundAssess.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSoundAssess : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_sound_assess, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleClick()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentSoundAssess.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentSoundAssess().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    fun handleClick() {
        val v = requireView()
        v.findViewById<ImageButton>(R.id.bt_sound_assessment_back).setOnClickListener {
            val result = Bundle().apply {
                putString("event", "Back")
            }
            parentFragmentManager.setFragmentResult("SoundAssess", result)
        }
        v.findViewById<TextView>(R.id.tv_sound_practice).setOnClickListener {
            val result = Bundle().apply {
                putString("event", "SoundPractice")
            }
            parentFragmentManager.setFragmentResult("SoundAssess", result)
        }
        v.findViewById<TextView>(R.id.tv_professional_assess).setOnClickListener {
            val result = Bundle().apply {
                putString("event", "ProfessionalAssess")
            }
            parentFragmentManager.setFragmentResult("SoundAssess", result)
        }
    }
}