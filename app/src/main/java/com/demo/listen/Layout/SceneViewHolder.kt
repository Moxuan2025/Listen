package com.demo.listen.Layout

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.R

class SceneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sceneName: TextView = itemView.findViewById(R.id.scene_name)
    val sceneDifficulty: TextView = itemView.findViewById(R.id.scene_difficult)

    fun bind(scene: Scene) {
        sceneName.text = scene.name
        sceneDifficulty.text = "难度系数:${scene.difficulty}"
    }
}