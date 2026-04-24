package com.demo.listen.Layout.MainPages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.Adapter.RankAdapter
import com.demo.listen.Layout.DataType.RankItem
import com.demo.listen.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RankFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankAdapter
    private val rankItems = mutableListOf<RankItem>()

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
        return inflater.inflate(R.layout.fragment_rank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadInitialData()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupRecyclerView() {
        recyclerView = requireView().findViewById<RecyclerView>(R.id.rank_content)
        adapter = RankAdapter(rankItems)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RankFragment.adapter
            // 可选：添加分割线
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }
    private fun loadInitialData() {
        // 模拟初始数据
        val initialData = listOf(
            RankItem(1, "###", R.drawable.avatar_default, 0),
            RankItem(2, "###", R.drawable.avatar_default, 0),
            RankItem(3, "###", R.drawable.avatar_default, 0),
            RankItem(4, "###", R.drawable.avatar_default, 0)
        )
        adapter.updateItems(initialData)
    }

    private fun addNewRankItem() {
        val newItem = RankItem(
            rank = rankItems.size + 1,
            name = "###",
            avatarRes = R.drawable.avatar_default,
            clockInDays = 0
        )
        adapter.addItem(newItem)

        // 滚动到新添加的位置
        recyclerView.smoothScrollToPosition(rankItems.size - 1)
    }

    fun updateItemById(itemId: Int, newItem: RankItem) {
        val index = rankItems.indexOfFirst { it.rank == itemId }
        if (index != -1) {
            rankItems[index] = newItem
        }
    }
}