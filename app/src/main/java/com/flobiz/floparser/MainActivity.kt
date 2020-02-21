package com.flobiz.floparser


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.net.URL


/**
 * @created-by: Akshay Arora
 * @date: 2020-02-08
 * @description:
 */


class MainActivity : AppCompatActivity() {

//    lateinit var viewModel: ContactsViewModel

//    private var working: Boolean = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val viewModel: ContactsViewModel by viewModels()

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val selectedfile: Uri? =
                data?.getData()
            //The uri with the location of the file
            Log.d("SelectedFile",selectedfile.toString())
            selectedfile?.let {
                viewModel.startSavingFromFile(it)

                val inputStream: InputStream? = contentResolver.openInputStream(it)
                Log.d("SelectedFile", "Size ${inputStream.toString()}")
            }


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainStarted", "started")

        //requesting permission if not granted
        checkAndRequestPermissions()

        //grtting reference to ViewModel

        val viewModel: ContactsViewModel by viewModels()

        viewModel.apply {

            //observing work status(CSV)
            csvWorkObserver().observe(this@MainActivity, Observer { listOfWorkInfo ->
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }

                // We only care about the one output status.
                // Every continuation has only one worker tagged TAG_OUTPUT
                val workInfo = listOfWorkInfo[0]

                if (workInfo.state.isFinished) {

                    //updating notification
                    showWorkFinished("CSV")
//                    working = false
                    Toast.makeText(
                        this@MainActivity,
                        "Contacts Stored Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

            //observing work status(API)
            apiWorkObserver().observe(this@MainActivity, Observer { listOfWorkInfo ->
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }

                // We only care about the one output status.
                // Every continuation has only one worker tagged TAG_OUTPUT
                val workInfo = listOfWorkInfo[0]

                if (workInfo.state.isFinished) {

                    //updating notification
                    showWorkFinished("API")
//                    working = false
                    Toast.makeText(
                        this@MainActivity,
                        "Contacts Stored Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }


        getContactsFromCsv.setOnClickListener {
            //check if already working
//            if (!working) {
//                working = true
                val url = urlText.text.toString()

                //checking if entered text is correct URL
                if (validUrl(url)) {
                    Toast.makeText(this, "Started storing contacts", Toast.LENGTH_LONG).show()
                    viewModel.startSavingFromUrl(url)
                } else {
                    Toast.makeText(this, "Url not in proper Format", Toast.LENGTH_LONG).show()
                }
//            } else {

                //work already in progress
                Toast.makeText(this, "Work in progress", Toast.LENGTH_LONG).show()
            }
//        }

        getAllContactsFromApi.setOnClickListener {

            //check if already working
//            if (!working) {
//                working = true
                viewModel.startSavingFromApi()
//            } else {

                //work already in progress
                Toast.makeText(this, "Work in progress", Toast.LENGTH_LONG).show()
            }


        selectFileFromInternalStorage.setOnClickListener {
            openFileDialog()
        }
//        }

    }


    /**
     * checks whether the app has permissions
     * to read and write contacts,
     * if not, ask for it explicitly
     */
    private fun checkAndRequestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
            22
        )
    }

    /**
     * checks whether a string is in correct URL form
     * @param url: text to check URL validity
     */

    private fun validUrl(url: String?) =
        try {
            URL(url).toURI()
            true
        } // If there was an Exception
        // while creating URL object
        catch (e: Exception) {
            false
        }


    /**
     * function to update notification status when work completes
     * @param mode: used to recognise mode of saving-API or CSV
     */

    private fun showWorkFinished(mode: String) {
        if (mode.equals("CSV")) {
            Log.d("ContactsSaved", "Saved CSV")
            makeStatusNotification("Contacts Saved", applicationContext, "CSV Saving")
        } else {
            Log.d("ContactsSaved", "Saved API")
            makeStatusNotification("Contacts Saved", applicationContext, "API Saving")
        }
    }

    fun openFileDialog() { //Read file in Internal Storage
        val intent = Intent()
            .setType("text/comma-separated-values")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123)
    }


}
