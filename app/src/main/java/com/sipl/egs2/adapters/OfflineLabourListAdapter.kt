package com.sipl.egs2.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs2.R
import com.sipl.egs2.database.model.LabourWithAreaNames
import com.sipl.egs2.interfaces.OnDocumentItemDeleteListener
import com.sipl.egs2.ui.gramsevak.OfflineViewLabourDetailsActivity
import com.sipl.egs2.ui.registration.LabourRegistrationEdit1

class OfflineLabourListAdapter(var list: List<LabourWithAreaNames>,var onDocumentItemDeleteListener: OnDocumentItemDeleteListener) : RecyclerView.Adapter<OfflineLabourListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val ivEdit=itemView.findViewById<ImageView>(R.id.ivEdit)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
        val ivView=itemView.findViewById<ImageView>(R.id.ivView)
        val ivDelete=itemView.findViewById<ImageView>(R.id.ivDelete)
        val layoutSyncFailed=itemView.findViewById<LinearLayout>(R.id.layoutSyncFailed)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_labour_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            if(list[position].isSyncFailed)
            {
                holder.layoutSyncFailed.visibility=View.VISIBLE
            }else{
                holder.layoutSyncFailed.visibility=View.GONE
            }

            holder.ivPhoto.setImageURI(Uri.parse(list[position].photo))
            holder.tvFullName.text = list[position]?.fullName ?: "Default"
            holder.tvMobile.text = list[position]?.mobile ?: "Default"
            val address="${list[position].districtName} ->${list[position].talukaName} ->${list[position].villageName}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text= list[position].mgnregaId
            holder.ivEdit.setOnClickListener {
                val intent= Intent(holder.itemView.context, LabourRegistrationEdit1::class.java)
                intent.putExtra("id",list[position].id.toString())
                holder.itemView.context.startActivity(intent)
            }
            holder.ivView.setOnClickListener {
                val intent= Intent(holder.itemView.context, OfflineViewLabourDetailsActivity::class.java)
                intent.putExtra("id",list[position].id.toString())
                holder.itemView.context.startActivity(intent)
            }

            holder.ivDelete.setOnClickListener {
                onDocumentItemDeleteListener.onItemDelete(list[position])
            }
        } catch (e: Exception) {
            Log.d("mytag","OfflineLabourListAdapter: ${e.message}",e)
        }

    }
    override fun getItemCount(): Int {
        return list.size
    }
}