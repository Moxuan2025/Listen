package com.demo.listen.Layout.Adapter

// WordFormationAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.DataType.Word
import com.demo.listen.R

class WordFormationAdapter(
    private var words: List<Word>,
    private val onWordClick: (Word, Int) -> Unit
) : RecyclerView.Adapter<WordFormationAdapter.WordViewHolder>() {

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordText: TextView = itemView.findViewById(R.id.word_text)

        fun bind(word: Word, position: Int) {
            wordText.text = word.text
            if (word.isSelected) {
                wordText.setBackgroundResource(R.drawable.bg_word_choosed_frame)
            } else {
                wordText.setBackgroundResource(R.drawable.bg_word_frame)
            }

            itemView.setOnClickListener {
                onWordClick(word, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(words[position], position)
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }

    fun updateWordAtPosition(position: Int, word: Word) {
        words = words.toMutableList().apply {
            set(position, word)
        }
        notifyItemChanged(position)
    }
}