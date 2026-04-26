package com.demo.listen.Layout.MainPages

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.demo.listen.Layout.LoginRegister.Login
import com.demo.listen.R
import com.demo.listen.net.SessionStore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvName = view.findViewById<TextView>(R.id.tv_fuser_uname)

        tvName.setOnClickListener {
            val name = SessionStore.name(requireContext())

            if (name.isEmpty()) {
                goLogin(view)
            } else {
                Toast.makeText(requireContext(), "已登录: $name", Toast.LENGTH_SHORT).show()
            }
        }

        // ========== 添加退出登录功能（长按退出） ==========
        tvName.setOnLongClickListener {
            val name = SessionStore.name(requireContext())
            if (name.isNotEmpty()) {
                // 清除本地存储的用户信息
                // 假设 SessionStore 提供 clear 方法，若无请根据实际 API 调整
                SessionStore.clear(requireContext())  // 清除所有存储字段
                // 若 SessionStore 没有 clear 方法，可逐个置空，例如：
                // SessionStore.setName(requireContext(), "")
                // SessionStore.setRole(requireContext(), "")
                // SessionStore.setToken(requireContext(), "")

                // 刷新显示文本
                tvName.text = "点击登录"
                Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "尚未登录", Toast.LENGTH_SHORT).show()
            }
            true  // 消费长按事件，不再传递
        }
        // ================================================
    }

    override fun onResume() {
        super.onResume()

        val name = SessionStore.name(requireContext())
        val role = SessionStore.role(requireContext())

        val tvName = requireView().findViewById<TextView>(R.id.tv_fuser_uname)

        if (name.isEmpty()) {
            tvName.text = "点击登录"
        } else {
            tvName.text = "$name ($role)"
            requireView().findViewById<TextView>(R.id.tv_level).text = "Lv.1"
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun goLogin(view: View) {
        Toast.makeText(requireContext(), "Login", Toast.LENGTH_SHORT).show()
        startActivity(Intent(requireContext(), Login::class.java))
    }
}