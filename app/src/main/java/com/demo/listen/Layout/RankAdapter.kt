package com.demo.listen.Layout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.R

class RankAdapter(private val items: MutableList<RankItem>) :
    RecyclerView.Adapter<RankAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.rank_rank)
        val vAvatar: View = itemView.findViewById(R.id.rank_avatar)
        val tvName: TextView = itemView.findViewById(R.id.rank_name)
        val tvScore: TextView = itemView.findViewById(R.id.rank_clockin_days)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rank_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRank.text = item.rank.toString()
        holder.tvName.text = item.name
        holder.vAvatar.setBackgroundResource(item.avatarRes)
        holder.tvScore.text = "打卡天数：${item.clockInDays}"

        // 前三名特殊样式
        when (item.rank) {
            1 -> holder.itemView.setBackgroundResource(R.drawable.bg_rank_first)
            2 -> holder.itemView.setBackgroundResource(R.drawable.bg_rank_second)
            3 -> holder.itemView.setBackgroundResource(R.drawable.bg_rank_third)
            else -> holder.itemView.setBackgroundResource(R.drawable.bg_rank_normal)
        }
    }

    override fun getItemCount() = items.size

    // 动态添加单个条目
    fun addItem(item: RankItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    // 动态添加多个条目
    fun addItems(newItems: List<RankItem>) {
        val startPos = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }

    // 更新整个列表（如从网络获取后重新排序）
    fun updateItems(newItems: List<RankItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // 删除条目
    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}