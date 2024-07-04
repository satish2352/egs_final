package com.sipl.egs2.ui.gramsevak.dashboard

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class ClusterMarkerObject(
    var id: String,
    var type: String,
    var name: String,
    var url: String="",
    var snippets:String,
    var latLng: LatLng,
    var zindex:Float=0.0f
):ClusterItem{
    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return title;
    }

    override fun getSnippet(): String? {
        return snippets;
    }

    override fun getZIndex(): Float? {
        return zIndex;
    }
}