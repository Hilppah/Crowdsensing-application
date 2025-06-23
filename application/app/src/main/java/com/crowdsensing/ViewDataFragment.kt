package com.crowdsensing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment

class ViewDataFragment :Fragment () {

    private lateinit var navToolBar: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewdata, container, false)

        val navItems = resources.getStringArray(R.array.spinner_items)
        navToolBar = view.findViewById(R.id.toolbar_spinner)
        val navAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, navItems)
        navAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        navToolBar.adapter = navAdapter

        navToolBar =view.findViewById(R.id.toolbar_spinner)

        navToolBar.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                if (selectedItem == "Measure") {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    return view
    }
}