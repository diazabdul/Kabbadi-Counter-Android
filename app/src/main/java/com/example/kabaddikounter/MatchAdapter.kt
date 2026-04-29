package com.example.kabaddikounter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.MatchRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MatchAdapter(private val onDeleteClick: (MatchRecord) -> Unit) :
    ListAdapter<MatchRecord, MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.match_item, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = getItem(position)
        holder.bind(match, onDeleteClick)
    }

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teamAName: TextView = itemView.findViewById(R.id.teamAName)
        private val teamBName: TextView = itemView.findViewById(R.id.teamBName)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(match: MatchRecord, onDeleteClick: (MatchRecord) -> Unit) {
            teamAName.text = match.teamAName
            teamBName.text = match.teamBName
            scoreText.text = "${match.scoreA} - ${match.scoreB}"
            
            deleteButton.setOnClickListener {
                onDeleteClick(match)
            }
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<MatchRecord>() {
        override fun areItemsTheSame(oldItem: MatchRecord, newItem: MatchRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MatchRecord, newItem: MatchRecord): Boolean {
            return oldItem == newItem
        }
    }
}