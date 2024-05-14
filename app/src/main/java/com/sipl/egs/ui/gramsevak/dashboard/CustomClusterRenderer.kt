package com.sipl.egs.ui.gramsevak.dashboard

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.sipl.egs.utils.CustomMarkerObject

class CustomClusterRenderer(
    private val context: Context,
    private val map: GoogleMap,
    clusterManager: ClusterManager<ClusterMarkerObject>
) : DefaultClusterRenderer<ClusterMarkerObject>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: ClusterMarkerObject,
        markerOptions: MarkerOptions
    ) {
        super.onBeforeClusterItemRendered(item!!, markerOptions!!)
        markerOptions?.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusterMarkerObject>): Boolean {
        return cluster?.size ?: 0 > 1
    }
}
