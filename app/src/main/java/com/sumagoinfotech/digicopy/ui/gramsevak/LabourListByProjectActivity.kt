package com.sumagoinfotech.digicopy.ui.gramsevak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityLabourListByProjectBinding
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.adapters.LabourListByProjectAdapter
import com.sumagoinfotech.digicopy.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LabourListByProjectActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLabourListByProjectBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourListByProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labour_list)
        binding.recyclerViewLabourList.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
       val project_id=intent.getStringExtra("id")
        getLabourDetails(project_id!!)
    }


    private fun getLabourDetails(project_id:String) {

        val apiService= ApiClient.create(this@LabourListByProjectActivity)
        apiService.getLaboursByProjectId(project_id = project_id).enqueue(object :
            Callback<LabourByMgnregaId> {
            override fun onResponse(
                call: Call<LabourByMgnregaId>,
                response: Response<LabourByMgnregaId>
            ) {
                if(response.isSuccessful){
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val list=response.body()?.data
                        var adapter= LabourListByProjectAdapter(list)
                        binding.recyclerViewLabourList.adapter=adapter
                    }else {
                        Toast.makeText(this@LabourListByProjectActivity, "No records found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else{
                    Toast.makeText(this@LabourListByProjectActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                Toast.makeText(this@LabourListByProjectActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}