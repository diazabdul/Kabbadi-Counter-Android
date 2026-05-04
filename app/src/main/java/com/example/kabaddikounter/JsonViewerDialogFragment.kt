package com.example.kabaddikounter

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.kabaddikounter.databinding.DialogJsonViewerBinding
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader

class JsonViewerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val uri = requireArguments().getParcelable<Uri>(ARG_URI)
            ?: error("Missing URI argument")

        val binding = DialogJsonViewerBinding.inflate(LayoutInflater.from(requireContext()))
        binding.json = readAndPrettyPrint(uri)

        return AlertDialog.Builder(requireContext())
            .setTitle("Downloaded JSON")
            .setView(binding.root)
            .setPositiveButton("Close") { d, _ -> d.dismiss() }
            .create()
    }

    private fun readAndPrettyPrint(uri: Uri): String {
        val raw = requireContext().contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        } ?: return "(empty file)"
        return runCatching {
            val element = JsonParser.parseString(raw)
            GsonBuilder().setPrettyPrinting().create().toJson(element)
        }.getOrDefault(raw)
    }

    companion object {
        const val TAG = "JsonViewerDialogFragment"
        private const val ARG_URI = "uri"

        fun newInstance(uri: Uri): JsonViewerDialogFragment =
            JsonViewerDialogFragment().apply {
                arguments = Bundle().apply { putParcelable(ARG_URI, uri) }
            }
    }
}
