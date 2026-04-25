package com.demo.listen.Layout.DataType

data class PracticeItem(
    val pinyin: String,         // 如 "ba"
    val words: List<String>     // 如 ["巴士", "拔河", "把手"]
) {
    // 还原为 "ba: 巴士 拔河 把手" 的形式
    fun formatString(): String = "$pinyin: ${words.joinToString(" ")}"
}