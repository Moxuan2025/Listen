package com.demo.listen.net

object PinyinUtils {

    /**
     * 将拼音文本构建为腾讯云 SSML 字符串
     * @param pinyin 可以是纯声母（如 "b"）、数字声调的拼音（如 "ba1"），直接放入 ph 属性
     */
    fun buildPinyinSsml(pinyin: String): String {
        return "<speak><phoneme alphabet=\"py\" ph=\"$pinyin\">$pinyin</phoneme></speak>"
    }

    /**
     * 将带声调符号的拼音转换为数字声调格式
     * 例如 bā → ba1, shuō → shuo1, lǜ → lv4
     * 如果转换失败，返回原字符串
     */
    fun convertToneMarkToNumber(toneMarkPinyin: String): String {
        // 声调映射：拼音字符 → 基础字母，以及对应的数字声调
        val toneMap = mapOf(
            'ā' to 'a', 'á' to 'a', 'ǎ' to 'a', 'à' to 'a',
            'ē' to 'e', 'é' to 'e', 'ě' to 'e', 'è' to 'e',
            'ī' to 'i', 'í' to 'i', 'ǐ' to 'i', 'ì' to 'i',
            'ō' to 'o', 'ó' to 'o', 'ǒ' to 'o', 'ò' to 'o',
            'ū' to 'u', 'ú' to 'u', 'ǔ' to 'u', 'ù' to 'u',
            'ǖ' to 'v', 'ǘ' to 'v', 'ǚ' to 'v', 'ǜ' to 'v'  // ü 转 v
        )

        val sb = StringBuilder()
        var toneNum = 5 // 默认轻声
        for (ch in toneMarkPinyin) {
            when {
                ch in toneMap -> {
                    sb.append(toneMap[ch])
                    toneNum = when (ch) {
                        'ā', 'ē', 'ī', 'ō', 'ū', 'ǖ' -> 1
                        'á', 'é', 'í', 'ó', 'ú', 'ǘ' -> 2
                        'ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ', 'ǚ' -> 3
                        'à', 'è', 'ì', 'ò', 'ù', 'ǜ' -> 4
                        else -> 5
                    }
                }
                else -> sb.append(ch)
            }
        }
        sb.append(toneNum)
        return sb.toString()
    }
}