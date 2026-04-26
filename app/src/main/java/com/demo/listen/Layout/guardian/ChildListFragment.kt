package com.demo.listen.Layout.guardian

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.demo.listen.R
import com.demo.listen.net.ServerApi
import kotlinx.coroutines.launch

class ChildListFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var btnAddChild: Button
    private val childDetails = mutableListOf<ServerApi.ChildInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_child_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.list_children)
        tvEmpty = view.findViewById(R.id.tv_empty)
        btnAddChild = view.findViewById(R.id.btn_add_child)

        btnAddChild.setOnClickListener {
            showAddChildDialog()
        }

        loadChildren()
    }

    private fun loadChildren() {
        lifecycleScope.launch {
            try {
                // [步骤 1 & 7] 确认调用并检查 Token
                val token = com.demo.listen.net.SessionStore.token(requireContext())
                Log.e("LOAD", "Starting to load children. Token length=${token.length}")
                
                // 1. 获取孩子列表（已包含 name）
                childDetails.clear()
                childDetails.addAll(ServerApi.getChildren())

                if (childDetails.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    listView.visibility = View.GONE
                    return@launch
                }

                tvEmpty.visibility = View.GONE
                listView.visibility = View.VISIBLE

                // 2. 直接使用返回的 name 字段显示
                val displayNames = childDetails.map { it.name }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    displayNames
                )
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val selectedChild = childDetails[position]
                    val intent = Intent(requireContext(), com.demo.listen.Layout.guardian.ChildDetailActivity::class.java).apply {
                        putExtra("child_username", selectedChild.username)
                    }
                    startActivity(intent)
                }
            } catch (e: Throwable) {
                // [步骤 6] 捕获所有异常并打印堆栈
                Log.e("LOAD", "Error loading children", e)
                Toast.makeText(requireContext(), "加载孩子列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddChildDialog() {
        val editText = EditText(requireContext())
        editText.hint = "请输入已注册的孩子用户名"
        AlertDialog.Builder(requireContext())
            .setTitle("关联已有孩子")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val childUsername = editText.text.toString().trim()
                if (childUsername.isNotEmpty()) {
                    addChild(childUsername)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addChild(childUsername: String) {
        lifecycleScope.launch {
            try {
                val resp = ServerApi.addChild(childUsername)
                if (resp.optBoolean("ok")) {
                    Toast.makeText(requireContext(), "关联成功", Toast.LENGTH_SHORT).show()
                    loadChildren() // 刷新列表
                } else {
                    Toast.makeText(requireContext(), "关联失败: ${resp.optString("message")}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "操作异常: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
