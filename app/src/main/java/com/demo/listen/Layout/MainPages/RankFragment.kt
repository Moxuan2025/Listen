package com.demo.listen.Layout.MainPages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.Adapter.RankAdapter
import com.demo.listen.Layout.DataType.RankItem
import com.demo.listen.R
import org.w3c.dom.Text

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
    private lateinit var clockInDays: TextView
    private lateinit var readModel: TextView
    private lateinit var progressModel: TextView
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

        mapWidget()
        setupRecyclerView()
        initPage()
        handleClick()

    }

    private fun mapWidget() {
        clockInDays = requireView().findViewById<TextView>(R.id.clock_in_days)
        readModel = requireView().findViewById<TextView>(R.id.read_model)
        progressModel = requireView().findViewById<TextView>(R.id.progress_model)
    }

    private fun initPage() {
        // TODO: Get data from server
        loadRankData(testClockInRank)   // test
    }

    private fun handleClick() {
        clockInDays.setOnClickListener {
            resetBackground()
            clockInDays.setBackgroundResource(R.drawable.bg_frank_navi_green)
            loadRankData(testClockInRank)
        }

        readModel.setOnClickListener {
            resetBackground()
            readModel.setBackgroundResource(R.drawable.bg_frank_navi_green)
            loadRankData(testReadRank)
        }
        progressModel.setOnClickListener {
            resetBackground()
            progressModel.setBackgroundResource(R.drawable.bg_frank_navi_green)
            loadRankData(testProgressRank)
        }
    }

    private fun resetBackground() {
        clockInDays.setBackgroundResource(R.drawable.bg_frank_navi_yellow)
        readModel.setBackgroundResource(R.drawable.bg_frank_navi_yellow)
        progressModel.setBackgroundResource(R.drawable.bg_frank_navi_yellow)
    }

    companion object {
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
            // 添加分割线
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    val testClockInRank = listOf(
        RankItem(1, "小七", R.drawable.avatar_default, 6),
        RankItem(2, "六六", R.drawable.avatar_man2, 5),
        RankItem(3, "可可", R.drawable.avatar_gril2, 3),
        RankItem(4, "聪聪", R.drawable.avatar_girl1, 1)
    )
    val testReadRank = listOf(
        RankItem(1, "六六", R.drawable.avatar_man2, 5),
        RankItem(2, "小七", R.drawable.avatar_default, 6),
        RankItem(3, "可可", R.drawable.avatar_gril2, 3),
        RankItem(4, "聪聪", R.drawable.avatar_girl1, 1)
    )
    val testProgressRank = listOf(
        RankItem(1, "可可", R.drawable.avatar_gril2, 3),
        RankItem(2, "六六", R.drawable.avatar_man2, 5),
        RankItem(3, "小七", R.drawable.avatar_default, 6),
        RankItem(4, "聪聪", R.drawable.avatar_girl1, 1)
    )
    private fun loadRankData(data: List<RankItem>) {
        adapter.updateItems(data)
    }

    private fun addNewRankItem() {
        val newItem = RankItem(
            rank = rankItems.size + 1,
            name = "###",
            avatarRes = R.drawable.avatar_default,
            clockInDays = 0
        )
        adapter.addItem(newItem)

        recyclerView.smoothScrollToPosition(rankItems.size - 1)
    }

    fun updateItemById(itemId: Int, newItem: RankItem) {
        val index = rankItems.indexOfFirst { it.rank == itemId }
        if (index != -1) {
            rankItems[index] = newItem
        }
    }
}