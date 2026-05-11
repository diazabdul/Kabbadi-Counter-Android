package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.databinding.FragmentHistoryBinding
import com.example.kabaddikounter.viewModels.ScoreViewModel

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ScoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(requireActivity())[ScoreViewModel::class.java]

        val adapter = MatchAdapter(
            onDeleteClick = { match -> viewModel.deleteMatch(match) },
            onLoadClick = { match -> viewModel.loadMatch(match) }
        )
        binding.historyRecyclerView.adapter = adapter
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.allMatches.observe(viewLifecycleOwner) { matches ->
            adapter.submitList(matches) {
                binding.historyRecyclerView.scrollToPosition(0)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
