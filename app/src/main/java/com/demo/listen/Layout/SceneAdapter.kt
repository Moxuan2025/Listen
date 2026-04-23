package com.demo.listen.Layout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.R

class SceneAdapter(
    private var scenes: List<Scene>,
    private val onItemClick: (Scene) -> Unit
) : RecyclerView.Adapter<SceneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SceneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scene, parent, false)
        return SceneViewHolder(view)
    }

    override fun onBindViewHolder(holder: SceneViewHolder, position: Int) {
        val scene = scenes[position]
        holder.bind(scene)

        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(scene)
        }
    }

    override fun getItemCount(): Int = scenes.size

    // 更新数据的方法
    fun updateData(newScenes: List<Scene>) {
        scenes = newScenes
        notifyDataSetChanged()
    }
}