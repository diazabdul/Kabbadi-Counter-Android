package com.example.kabaddikounter.ui.backend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.databinding.ItemRemoteMatchBinding

class BackendMatchAdapter : ListAdapter<RemoteMatchDto, BackendMatchAdapter.MatchViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemRemoteMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MatchViewHolder(private val binding: ItemRemoteMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RemoteMatchDto) {
            binding.titleText.text = "${item.teamAName} vs ${item.teamBName}"
            binding.scoreText.text = "${item.teamAScore} - ${item.teamBScore}"
            binding.statusText.text = item.status
        }
    }

    class Diff : DiffUtil.ItemCallback<RemoteMatchDto>() {
        override fun areItemsTheSame(oldItem: RemoteMatchDto, newItem: RemoteMatchDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RemoteMatchDto, newItem: RemoteMatchDto): Boolean {
            return oldItem == newItem
        }
    }
}
