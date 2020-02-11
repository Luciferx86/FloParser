package com.flobiz.floparser


import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

/**
 * @created-by: Akshay Arora
 * @date: 2020-02-08
 * @description:
 */


class MainActivity : AppCompatActivity() {

    lateinit var viewModel: ContactsViewModel

    private var working: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainStarted","started")

        //requesting permission if not granted
        checkAndRequestPermissions()

        //grtting reference to ViewModel
        viewModel = ViewModelProviders.of(this).get(ContactsViewModel::class.java)


        viewModel.apply {

            //observing work status(CSV)
            csvWorkObserver().observe(this@MainActivity, Observer { listOfWorkInfo->
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }

                // We only care about the one output status.
                // Every continuation has only one worker tagged TAG_OUTPUT
                val workInfo = listOfWorkInfo[0]

                if (workInfo.state.isFinished) {

                    //updating notification
                    showWorkFinished("CSV")
                    working = false
                    Toast.makeText(this@MainActivity,"Contacts Stored Successfully",Toast.LENGTH_LONG).show()
                }
            })

            //observing work status(API)
            apiWorkObserver().observe(this@MainActivity, Observer { listOfWorkInfo->
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }

                // We only care about the one output status.
                // Every continuation has only one worker tagged TAG_OUTPUT
                val workInfo = listOfWorkInfo[0]

                if (workInfo.state.isFinished) {

                    //updating notification
                    showWorkFinished("API")
                    working = false
                    Toast.makeText(this@MainActivity,"Contacts Stored Successfully",Toast.LENGTH_LONG).show()
                }
            })
        }


        getContactsFromCsv.setOnClickListener {
            //check if already working
            if(!working) {
                working = true
                val url = urlText.text.toString()

                //checking if entered text is correct URL
                if (validUrl(url)) {
                    Toast.makeText(this, "Started storing contacts", Toast.LENGTH_LONG).show()
                    viewModel.startSavingFromUrl(url)
                } else {
                    Toast.makeText(this, "Url not in proper Format", Toast.LENGTH_LONG).show()
                }
            }else{

                //work already in progress
                Toast.makeText(this, "Work in progress", Toast.LENGTH_LONG).show()
            }
        }

        getAllContactsFromApi.setOnClickListener{

            //check if already working
            if(!working) {
                working = true
                viewModel.startSavingFromApi()
            }else{

                //work already in progress
                Toast.makeText(this, "Work in progress", Toast.LENGTH_LONG).show()
            }
        }

    }


    /**
     * checks whether the app has permissions
     * to read and write contacts,
     * if not, ask for it explicitly
     */
    private fun checkAndRequestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
            22)
    }

    /**
     * checks whether a string is in correct URL form
     * @param url: text to check URL validity
     */

    private fun validUrl(url: String?): Boolean { /* Try creating a valid URL */
        return try {
            URL(url).toURI()
            true
        } // If there was an Exception
        // while creating URL object
        catch (e: Exception) {
            false
        }
    }

    /**
     * function to update notification status when work completes
     * @param mode: used to recognise mode of saving-API or CSV
     */

    private fun showWorkFinished(mode:String) {
        if(mode.equals("CSV")) {
            Log.d("ContactsSaved", "Saved CSV")
            makeStatusNotification("Contacts Saved", applicationContext, "CSV Saving")
        }else{
            Log.d("ContactsSaved", "Saved API")
            makeStatusNotification("Contacts Saved", applicationContext, "API Saving")
        }
    }

}
