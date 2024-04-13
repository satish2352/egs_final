package com.sumagoinfotech.digicopy.ui.gramsevak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.LaboursSentForApprovalAdapter
import com.sumagoinfotech.digicopy.databinding.ActivityViewLabourSentForApprovalBinding
import com.sumagoinfotech.digicopy.model.apis.labourlist.LabourListModel
import com.sumagoinfotech.digicopy.model.apis.labourlist.LaboursList
import com.sumagoinfotech.digicopy.pagination.MyPaginationAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewLaboursListSentForApprovalActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding:ActivityViewLabourSentForApprovalBinding
    private  lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter:LaboursSentForApprovalAdapter
    private lateinit var labourList:ArrayList<LaboursList>
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage=""
    private lateinit var paginationLayoutManager : LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding=ActivityViewLabourSentForApprovalBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.sent_for_approval)
            apiService=ApiClient.create(this)
            dialog= CustomProgressDialog(this)
            labourList= ArrayList()
            adapter= LaboursSentForApprovalAdapter(labourList)
            paginationAdapter= MyPaginationAdapter(0,"0",this)
            binding.recyclerViewPageNumbers.adapter=adapter
            binding.recyclerView.adapter=adapter
            binding.recyclerViewPageNumbers.layoutManager= LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            paginationLayoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
            binding.recyclerView.layoutManager=paginationLayoutManager
            currentPage="1"
            //getDataFromServer()
        } catch (e: Exception) {
            Log.d("mytag","ViewLabourSentForApprovalActivity : onCreate : Exception => "+e.message)
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        getDataFromServer(currentPage)
    }
    private fun getDataFromServer(currentPage: String) {
        try {
            dialog.show()
            val call=apiService.getLaboursListSentForApproval(startPageNumber = currentPage)
            call.enqueue(object : Callback<LabourListModel>{
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful)
                    {
                        if(response.body()?.status.equals("true"))
                        {
                            if(!response?.body()?.data.isNullOrEmpty())
                            {
                                labourList= (response?.body()?.data as ArrayList<LaboursList>?)!!
                                adapter= LaboursSentForApprovalAdapter(labourList)
                                binding.recyclerView.adapter=adapter
                                adapter.notifyDataSetChanged()
                                val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@ViewLaboursListSentForApprovalActivity)
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()

                            }else{
                                labourList= (response?.body()?.data as ArrayList<LaboursList>?)!!
                                adapter= LaboursSentForApprovalAdapter(labourList)
                                binding.recyclerView.adapter=adapter
                                adapter.notifyDataSetChanged()
                                val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@ViewLaboursListSentForApprovalActivity)
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()
                            }

                        }else{
                            Toast.makeText(this@ViewLaboursListSentForApprovalActivity,resources.getString(R.string.please_try_again),Toast.LENGTH_SHORT).show()
                        }
                    }else{

                        Toast.makeText(this@ViewLaboursListSentForApprovalActivity,resources.getString(R.string.please_try_again),Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LabourListModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@ViewLaboursListSentForApprovalActivity,resources.getString(R.string.error_occured_during_api_call),Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
          runOnUiThread {   dialog.dismiss()
              Toast.makeText(this@ViewLaboursListSentForApprovalActivity,resources.getString(R.string.please_try_again),Toast.LENGTH_SHORT).show() }
            Log.d("mytag","ViewLabourSentForApprovalActivity : getDataFromServer : Exception => "+e.message)
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        Log.d("mytag","ListActivity :: getDataFromServer "+pageNumber)

            getDataFromServer("$pageNumber")
            paginationAdapter.setSelectedPage(pageNumber)
            currentPage="$pageNumber"

    }
}