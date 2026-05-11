package com.example.kabaddikounter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.MatchRecord
import com.example.kabaddikounter.databinding.MatchItemBinding

class MatchAdapter(
    private val onDeleteClick: (MatchRecord) -> Unit,
    private val onLoadClick: (MatchRecord) -> Unit
) :
    ListAdapter<MatchRecord, MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = MatchItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick, onLoadClick)
    }

    class MatchViewHolder(private val binding: MatchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            match: MatchRecord,
            onDeleteClick: (MatchRecord) -> Unit,
            onLoadClick: (MatchRecord) -> Unit
        ) {
            binding.match = match
            binding.onDelete = android.view.View.OnClickListener { onDeleteClick(match) }
            binding.onLoad = android.view.View.OnClickListener { onLoadClick(match) }
            binding.executePendingBindings()
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<MatchRecord>() {
        override fun areItemsTheSame(oldItem: MatchRecord, newItem: MatchRecord): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MatchRecord, newItem: MatchRecord): Boolean =
            oldItem == newItem
    }
}
