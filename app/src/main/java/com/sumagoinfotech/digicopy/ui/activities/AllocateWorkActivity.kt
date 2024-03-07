package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityAllocateWorkBinding

class AllocateWorkActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAllocateWorkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAllocateWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.allocate_work)

        val names = listOf("Nashik I", "Pune II ", "Mumbai")
        val atvNamesAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, names
        )
        val projectArea:AutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.projectArea)
        projectArea.setAdapter(atvNamesAdapter)
        projectArea.setOnFocusChangeListener{abaad,asd ->
            projectArea.showDropDown()
        }
        projectArea.setOnClickListener{
            projectArea.showDropDown()
        }


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}