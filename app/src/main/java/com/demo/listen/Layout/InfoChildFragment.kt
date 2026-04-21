package com.demo.listen.Layout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfoChildFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfoChildFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var spGender: Spinner
    private lateinit var spEar: Spinner
    private lateinit var spDevice: Spinner
    private lateinit var spDeviceTime: Spinner
    private lateinit var spStartTime: Spinner

    private var choices: Array<String>? = null

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
        return inflater.inflate(R.layout.fragment_info_child, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        requireView().findViewById<Button>(R.id.bt_info_child_next).setOnClickListener {
            goNext()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InfoChildFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoChildFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupSpinners() {
        spGender = requireView().findViewById<Spinner>(R.id.sp_gender)
        var genderAd = ArrayAdapter.createFromResource(requireContext(),
            R.array.sa_gender,
            android.R.layout.simple_spinner_item)
        genderAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGender.setAdapter(genderAd)
        // TODO: rest spinners
    }

    private fun goNext() {
        val result = Bundle().apply {
            putStringArray("choices", choices)
        }
        parentFragmentManager.setFragmentResult("infoChild", result)
    }
}