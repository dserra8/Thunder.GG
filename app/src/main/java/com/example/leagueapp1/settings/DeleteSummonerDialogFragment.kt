package com.example.leagueapp1.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteSummonerDialogFragment : DialogFragment() {

    private val viewModel: DeleteSummonerViewModel by viewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do you really want to delete this Summoner?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes"){_,_ ->
                viewModel.onConfirmClick()
            }
            .create()
}