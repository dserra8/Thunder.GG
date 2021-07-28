package com.example.leagueapp1.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.leagueapp1.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteSummonerDialogFragment : DialogFragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do you really want to delete this Summoner?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes"){_,_ ->
                mainViewModel.isSummonerActive(false)
                viewModel.onConfirmClick()
            }
            .create()

    companion object {
        const val TAG = "DeleteSummonerDialog"
    }
}