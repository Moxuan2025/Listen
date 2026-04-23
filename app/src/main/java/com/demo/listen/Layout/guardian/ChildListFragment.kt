package com.demo.listen.Layout.guardian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.demo.listen.R
import com.demo.listen.net.ServerApi
import kotlinx.coroutines.launch

class ChildListFragment : Fragment() {

    private lateinit var listView: ListView
    private val childNames = mutableListOf<String>()

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

        // 预留：模拟数据（后续替换为真实 API 调用）
        // 实际应该调用 ServerApi.getChildren(guardianUsername)
        loadChildren()
    }

    private fun loadChildren() {
        lifecycleScope.launch {
            try {
                // TODO: 替换为真实 API 调用
                // val children = ServerApi.getChildren(guardianUsername)
                // 暂时用假数据展示
                val fakeChildren = listOf("小明 (child1)", "小红 (child2)")
                childNames.clear()
                childNames.addAll(fakeChildren)

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    childNames
                )
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val childName = childNames[position].split(" ")[0]
                    // 跳转到孩子详情页（预留接口）
                    Toast.makeText(requireContext(), "查看孩子: $childName", Toast.LENGTH_SHORT).show()
                    // TODO: startActivity(Intent(requireContext(), ChildDetailActivity::class.java).putExtra("child_username", childName))
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加载孩子列表失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}