package com.example.kabaddikounter.ui.backend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.BuildConfig
import com.example.kabaddikounter.databinding.ActivityBackendTestBinding

class BackendTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackendTestBinding
    private val viewModel: BackendTestViewModel by viewModels()
    private lateinit var adapter: BackendMatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackendTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Backend Test"

        adapter = BackendMatchAdapter()
        binding.matchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.matchRecyclerView.adapter = adapter

        binding.baseUrlText.text = "Base URL: ${BuildConfig.BASE_URL}"
        binding.refreshButton.setOnClickListener { viewModel.refresh() }

        viewModel.uiState.observe(this) { state ->
            renderState(state)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun renderState(state: BackendUiState) {
        when (state) {
            BackendUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = "Loading matches..."
                binding.matchRecyclerView.visibility = View.GONE
            }

            BackendUiState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = "No matches from server"
                binding.matchRecyclerView.visibility = View.GONE
            }

            is BackendUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = "Error: ${state.message}"
                binding.matchRecyclerView.visibility = View.GONE
            }

            is BackendUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.visibility = View.GONE
                binding.matchRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.matches)
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, BackendTestActivity::class.java)
    }
}
