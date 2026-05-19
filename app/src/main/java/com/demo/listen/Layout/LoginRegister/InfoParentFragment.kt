package com.demo.listen.Layout.LoginRegister

import android.os.Bundle
import android.util.Log
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
 * Use the [InfoParentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfoParentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var choices: Array<String>? = null
    
    private lateinit var spSoundLose: Spinner
    private lateinit var spSpeak: Spinner
    private lateinit var spDevice: Spinner
    private lateinit var spListenSpeak: Spinner
    private lateinit var spReserve: Spinner

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
        return inflater.inflate(R.layout.fragment_info_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("INFO_PARENT", "=== InfoParentFragment onViewCreated 开始 ===")
        setupSpinners()
        requireView().findViewById<Button>(R.id.bt_info_parent_next).setOnClickListener {
            Log.e("INFO_PARENT", "点击下一步按钮")
            goNext()
        }
    }

    private fun setupSpinners() {
        Log.e("INFO_PARENT", "=== 开始设置 Spinners ===")
        
        spSoundLose = requireView().findViewById<Spinner>(R.id.sp_sound_lose)
        Log.e("INFO_PARENT", "sp_sound_lose 找到: ${spSoundLose != null}")
        
        // 检查是否有对应的字符串数组资源
        try {
            val soundLoseOptions = resources.getStringArray(R.array.sa_hearing_loss_level)
            Log.e("INFO_PARENT", "听力损失等级选项数量: ${soundLoseOptions.size}")
            soundLoseOptions.forEachIndexed { index, s ->
                Log.e("INFO_PARENT", "  选项[$index]: $s")
            }
            
            val adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.sa_hearing_loss_level,
                android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSoundLose.adapter = adapter
            Log.e("INFO_PARENT", "sp_sound_lose 适配器设置完成")
        } catch (e: Exception) {
            Log.e("INFO_PARENT", "设置 sp_sound_lose 失败: ${e.message}")
            e.printStackTrace()
        }
        
        // TODO: 设置其他 Spinners
        spSpeak = requireView().findViewById<Spinner>(R.id.sp_speak)
        spDevice = requireView().findViewById<Spinner>(R.id.sp_device)
        spListenSpeak = requireView().findViewById<Spinner>(R.id.sp_listen_speak)
        spReserve = requireView().findViewById<Spinner>(R.id.sp_reserve)
        
        Log.e("INFO_PARENT", "=== Spinners 设置完成 ===")
    }

    private fun goNext() {
        Log.e("INFO_PARENT", "=== goNext 方法被调用 ===")
        
        // 收集所有 Spinner 的选择
        choices = arrayOf(
            spSoundLose.selectedItem?.toString() ?: "未选择",
            spSpeak.selectedItem?.toString() ?: "未选择",
            spDevice.selectedItem?.toString() ?: "未选择",
            spListenSpeak.selectedItem?.toString() ?: "未选择",
            spReserve.selectedItem?.toString() ?: "未选择"
        )
        
        Log.e("INFO_PARENT", "收集到的选择:")
        choices?.forEachIndexed { index, s ->
            Log.e("INFO_PARENT", "  choices[$index]: $s")
        }
        
        val result = Bundle().apply {
            putStringArray("choices", choices)
        }
        Log.e("INFO_PARENT", "发送 FragmentResult: infoParent")
        parentFragmentManager.setFragmentResult("infoParent", result)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InfoParentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoParentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}