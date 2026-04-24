package com.demo.listen.Layout.EnjoyStudy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.Scene
import com.demo.listen.Layout.Adapter.SceneAdapter
import com.demo.listen.R

class SceneSelect : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SceneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scene_select)

        initRecyclerView()
        loadScenes()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.rv_scenes)

        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 创建适配器
        adapter = SceneAdapter(emptyList()) { scene ->
            onSceneSelected(scene)
        }

        recyclerView.adapter = adapter
    }

    private fun loadScenes() {
        // 模拟从网络或本地数据库获取数据
        val scenes = listOf(
            Scene(1, "居家", 3),
            Scene(2, "日常游玩", 4),
            Scene(3, "情绪表达", 5),
            Scene(4, "购物", 2),
            Scene(5, "生活服务", 3),
            Scene(6, "兴趣爱好", 3)
        )

        adapter.updateData(scenes)
    }

    private fun onSceneSelected(scene: Scene) {
        // 处理场景选择逻辑
        // 例如：跳转到对应的学习界面
        // Toast.makeText(this, "选择了: ${scene.name}", Toast.LENGTH_SHORT).show()

        // 可以在这里添加您的业务逻辑
        // startActivity(Intent(this, SceneDetailActivity::class.java).apply {
        //     putExtra("scene_id", scene.id)
        //     putExtra("scene_name", scene.name)
        // })
    }
}