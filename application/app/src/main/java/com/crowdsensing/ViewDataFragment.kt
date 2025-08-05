package com.crowdsensing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.crowdsensing.ViewUtils.setupNavigationSpinner

class ViewDataFragment :Fragment () {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewdata, container, false)

        val navItems = resources.getStringArray(R.array.spinner_items)
        val navToolBar: Spinner = view.findViewById(R.id.toolbar_spinner)

        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            when (selectedItem) {
                "Measure" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                }
            }
        }
        return view
    }
}