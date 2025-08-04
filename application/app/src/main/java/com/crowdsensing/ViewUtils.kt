package com.crowdsensing

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView

object ViewUtils {
    fun setupDropdownMenu(
        context: Context,
        dropdown: MaterialAutoCompleteTextView,
        items: Array<String>,
        backgroundDrawableRes: Int = R.drawable.dropdown
    ) {
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            items
        )

        dropdown.setAdapter(adapter)
        dropdown.setDropDownBackgroundDrawable(ContextCompat.getDrawable(context, backgroundDrawableRes))
        dropdown.setOnClickListener { dropdown.showDropDown() }
    }

    fun setupSpinner(
        context: Context,
        spinner: Spinner,
        itemsResId: Int
    ) {
        val items = context.resources.getStringArray(itemsResId)
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    fun updateSwitchColors(switch: Switch, isChecked: Boolean, context: Context) {
        val thumbColor = if (isChecked) R.color.colorSwitchOn else R.color.colorSwitchOff
        val trackColor = if (isChecked) R.color.colorTrackOn else R.color.colorTrackOff
        switch.thumbTintList = ContextCompat.getColorStateList(context, thumbColor)
        switch.trackTintList = ContextCompat.getColorStateList(context, trackColor)
    }

    fun setupNavigationSpinner(
        spinner: Spinner,
        items: Array<String>,
        onItemSelected: (String) -> Unit
    ) {
        val adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        var isFirstSelection = true

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                val selected = parent.getItemAtPosition(position) as String
                if (selected != "Select...") {
                    onItemSelected(selected)
                }
                spinner.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}