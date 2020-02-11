package com.flobiz.floparser.workers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.flobiz.floparser.makeStatusNotification
import com.opencsv.CSVReader
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import kotlin.collections.ArrayList

/**
 * @created-by: Akshay Arora
 * @date: 2020-02-08
 * @description: Worker class for storing contacts from CSV file
 */

class CsvContactsWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val processCompleteLive: MutableLiveData<String>? = null


    override fun doWork(): Result {

        return try {

            //getting url to CSV file
            val url= inputData.getString("url")
            makeStatusNotification("Saving Contacts from CSV", applicationContext, "CSV Saving")


            //creating data object from CSV results
            val myData = createDataFromArray(readCSV(url))

            //MAIN METHOD CALL

            removeAllContacts()


            storeContacts(
                myData.getStringArray("allNames"),
                myData.getStringArray("allNumbers")
            )

            val x = Data.Builder()
            Result.success(x.build())

        } catch (e: Exception) {
            Log.e("NoWork","Unable to save image to Gallery $e")
            Result.failure()
        }

    }

    /**
     * function to iterate over all the names and numbers, and add individual contact
     * @param allNames: an array of all the names
     * @param allNumbers: an array of all the numbers
     */
    private fun storeContacts(allNames: Array< out String >?, allNumbers : Array<out String>?){

        val size = allNames?.size.toString()

        val length = size.toInt()

        for(x in 0..length-1){
            val name = allNames?.get(x)
            val number = allNumbers?.get(x)
            Log.d(
                "RowVal2",
                "Name: $name Number: $number"
            )
            val builder = Data.Builder()
            builder.putString("name", name)
            builder.putString("phone", number)
            val saveRequest = OneTimeWorkRequestBuilder<SaveWorker>()
                .setInputData(builder.build())
                .build()
            val workManager: WorkManager = WorkManager.getInstance(applicationContext)
            workManager.enqueue(saveRequest)
            Thread.sleep(50)
        }
    }

    /**
     * function to remove all existing contacts from contact list
     */
    private fun removeAllContacts(){

        var removed = 0

        val contentResolver: ContentResolver = applicationContext.getContentResolver()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            removed++
            val lookupKey =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                lookupKey
            )
            Log.d("DeleteContact","deleted $removed")
            contentResolver.delete(uri, null, null)
        }
        if(removed == 0){
            Log.d("DeleteContacts","Nothing to Delete")
        }else{
            Log.d("DeleteContacts", "Deleted $removed contacts")
        }
        cursor.close()
    }


    /**
     * function to create Data object from Array containing all names and numbers
     */
    private fun createDataFromArray(allData:List<Array<String>?>?): Data {

        val allNames = object : ArrayList<String>(){}
        val allNumbers = object : ArrayList<String>(){}
        val builder = Data.Builder()
        val myItr = allData?.iterator()
        while (myItr?.hasNext() == true){
            val x = myItr.next()
            x?.get(0)?.also { it -> allNames.add(it) }
            x?.get(1)?.also { it -> allNumbers.add(it) }
        }
        builder.putStringArray("allNames",allNames.toTypedArray())
        builder.putStringArray("allNumbers",allNumbers.toTypedArray())
        return builder.build()
    }

    /**
     * function to open input stream from the url, read the csv, and parse it into an ArrayList object
     * @param url: url to open input stream
     */
    private fun readCSV(url: String?): List<Array<String>?>?{

        val rows  = object : ArrayList<Array<String>?>(){}

//        val myStream: InputStream = thisApplication.assets.open("ct1.csv")
        val input: InputStream = URL(url).openStream()
        val isr = InputStreamReader(input)
        val br = BufferedReader(isr)
        var reader = CSVReader(br,',')


        val records = reader.readAll()

        val iterator: Iterator<Array<String>> = records.iterator()

        iterator.next()
        iterator.next()

        while (iterator.hasNext()) {
            val record = iterator.next()
            val name = record[1]
            val number  = record[2]
            if(name.isEmpty()){
                Log.d("NewRecord","Name Empty")
            }else{
                Log.d("NewRecord",name)
            }
            Log.d("NewRecord",number)

            var myRow = arrayOf(name,number)
            rows.add(myRow)
        }

        return rows
    }


}
