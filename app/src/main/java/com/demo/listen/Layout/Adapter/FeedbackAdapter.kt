package com.demo.listen.Layout.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.DataType.FeedbackItem
import com.demo.listen.R

class FeedbackAdapter(private val items: List<FeedbackItem>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvFeedbackTarget.text = item.target
        holder.tvFeedbackTimes.text = item.title
        holder.tvFeedbackScore.text = item.score
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFeedbackTarget: TextView = itemView.findViewById(R.id.tv_item_target)
        val tvFeedbackTimes: TextView = itemView.findViewById(R.id.tv_feedback_times)
        val tvFeedbackScore: TextView = itemView.findViewById(R.id.tv_feedback_score)
    }
}