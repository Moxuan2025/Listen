package com.demo.listen.Layout.EnjoyStudy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
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

    private lateinit var video: VideoView
    private lateinit var score: TextView
    private lateinit var playRecord: ImageView      // 播放录音
    private lateinit var spellAction: TextView
    private lateinit var record: ImageButton        // 录音
    private lateinit var preTone: TextView
    private lateinit var nxtTone: TextView


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
        return inflater.inflate(R.layout.fragment_practice_content,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapWidget()
    }


    private fun mapWidget() {
        video = requireView().findViewById<VideoView>(R.id.example_video)
        score = requireView().findViewById<TextView>(R.id.practice_score)
        playRecord = requireView().findViewById<ImageView>(R.id.example_sound_play)
        spellAction = requireView().findViewById<TextView>(R.id.spell_action)
        record = requireView().findViewById<ImageButton>(R.id.practise_sound_record)
        preTone = requireView().findViewById<TextView>(R.id.pc_pre)
        nxtTone = requireView().findViewById<TextView>(R.id.pc_nxt)
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