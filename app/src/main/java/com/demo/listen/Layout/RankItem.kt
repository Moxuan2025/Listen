package com.demo.listen.Layout

data class RankItem(
    val rank: Int,                  // 排名
    val name: String,               // 名字
    val avatarRes: Int,             // 头像资源ID（或图片URL）
    val clockInDays: Int,           // 分数/数据
    val extraData: String = ""      // 其他数据
)