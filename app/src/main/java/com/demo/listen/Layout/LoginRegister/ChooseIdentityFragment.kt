package com.demo.listen.Layout.LoginRegister

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChooseIdentityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChooseIdentityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var chooseChild: Boolean? = null

    private var llParent: LinearLayout? = null
    private var llChild: LinearLayout? = null

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
        return inflater.inflate(R.layout.fragment_choose_identity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 选择父母身份的点击事件
        requireView().findViewById<LinearLayout>(R.id.ll_parent).setOnClickListener {
            if (chooseChild == true || chooseChild == null) {
                it.setBackgroundResource(R.drawable.light_gray__opacity_bg)
                chooseChild = false
                requireView().findViewById<LinearLayout>(R.id.ll_child).setBackgroundResource(
                    R.drawable.white_bg)
            }
         }
        // 选择孩子身份的点击事件
        requireView().findViewById<LinearLayout>(R.id.ll_child).setOnClickListener {
            if (chooseChild == false || chooseChild == null) {
                it.setBackgroundResource(R.drawable.light_gray__opacity_bg)
                chooseChild = true
                requireView().findViewById<LinearLayout>(R.id.ll_parent).setBackgroundResource(
                    R.drawable.white_bg)
            }
        }
        // 下一个
        requireView().findViewById<Button>(R.id.bt_identity_next).setOnClickListener {
            if (chooseChild == null) {
                Toast.makeText(requireContext(), "请选择您的角色", Toast.LENGTH_SHORT).show()
            } else {
                var choice: String = if (chooseChild == true) "child" else "parent"
                val result = Bundle().apply {
                    putString("choice", choice)
                }
                parentFragmentManager.setFragmentResult("identity", result)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChooseIdentityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChooseIdentityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}