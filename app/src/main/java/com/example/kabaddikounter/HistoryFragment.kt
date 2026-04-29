package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.viewModels.ScoreViewModel

class HistoryFragment : Fragment() {
    private lateinit var viewModel: ScoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // FIX: R.id.historyRecyclerView is a RecyclerView in fragment_history.xml
        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        val adapter = MatchAdapter { match ->
            viewModel.deleteMatch(match)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Use the activity-scoped ViewModel so it's shared with MainActivity
        viewModel = ViewModelProvider(requireActivity()).get(ScoreViewModel::class.java)

        viewModel.allMatches.observe(viewLifecycleOwner) { matches ->
            adapter.submitList(matches){
            recyclerView.scrollToPosition(0)
            }
        }

        return view
    }
}