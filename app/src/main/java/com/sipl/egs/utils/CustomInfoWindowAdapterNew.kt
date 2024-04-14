package com.sipl.egs.utils

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapterNew(
    private val context: Context,
    private val snippets: List<String>
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
      /* val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        val snippetListView = view.findViewById<ListView>(R.id.snippetListView)

        // Populate the ListView with snippet data
        val adapter = ArrayAdapter(context, R.layout.simple_list_item_1, snippets)
        snippetListView.adapter = adapter*/

        return null
    }
}
