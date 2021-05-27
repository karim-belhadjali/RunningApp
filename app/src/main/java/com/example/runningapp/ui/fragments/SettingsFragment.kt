package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.other.Constants
import com.example.runningapp.other.Constants.CHANGES_MADE_SUCCESSFULLY
import com.example.runningapp.other.Constants.ENTER_ALL_VALUES
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.fragment_setup.etName
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSharedPreferences()
        btnApplyChanges.setOnClickListener {
            if (modifyUserDetailsInSharedPreferences()) {
                Snackbar.make(requireView(), CHANGES_MADE_SUCCESSFULLY, Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                Snackbar.make(requireView(), ENTER_ALL_VALUES, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSharedPreferences() {
        etNamee.setText(sharedPreferences.getString(KEY_NAME, ""))
        etWeight.setText( sharedPreferences.getFloat(KEY_WEIGHT, 90f).toString())
    }

    private fun modifyUserDetailsInSharedPreferences(): Boolean {
        val name = etNamee.text.toString()
        val weight = etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
            .putBoolean(Constants.KEY_TOGGLE_FIRST_TIME, false)
            .apply()

        val toolbarText = "Let's go $name"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}