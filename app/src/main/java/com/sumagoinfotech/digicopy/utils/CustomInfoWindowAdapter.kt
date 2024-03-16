package com.sumagoinfotech.digicopy.utils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.sumagoinfotech.digicopy.R

class CustomInfoWindowAdapter(private val inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to use default rendering if getInfoContents returns null
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        // Inflate custom layout for the info window
        val view = inflater.inflate(R.layout.custom_info_window, null)

        // Retrieve views from the custom layout
        val titleView = view.findViewById<TextView>(R.id.infoWindowTitle)
        val snippetView = view.findViewById<TextView>(R.id.infoWindowSnippet)

        // Set title and snippet for the info window
        titleView.text = marker.title
        snippetView.text = marker.snippet

        return view
    }
}
