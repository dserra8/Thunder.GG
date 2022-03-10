package com.example.leagueapp1.feature_settings.presentation.delete_summoner

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.leagueapp1.feature_settings.presentation.main_settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteSummonerDialogFragment : DialogFragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do you really want to delete this Summoner?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes"){_,_ ->
                viewModel.onConfirmClick()
            }
            .create()

    companion object {
        const val TAG = "DeleteSummonerDialog"
    }
}