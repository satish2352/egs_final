package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.sumagoinfotech.digicopy.R

class AllocateWorkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allocate_work)
        val names = listOf("John", "Doe", "David", "Jim", "Dan")
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
}