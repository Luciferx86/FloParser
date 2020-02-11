package com.flobiz.floparser.workers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.work.*
import com.flobiz.floparser.Model.ContactList
import com.flobiz.floparser.Rest.RestApi
import com.flobiz.floparser.makeStatusNotification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * @created-by: Akshay Arora
 * @date: 2020-02-08
 * @description: Worker class for storing contacts from API
 */
class ApiContactsWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private var retrofit: Retrofit? = null

    override fun doWork(): Result {

        return try {

            makeStatusNotification("Saving Contacts from API", applicationContext, "API Saving")

            //removing all existing contacts
            removeAllContacts()

            //
            connectApiAndStore()


            Result.success()

        } catch (e: Exception) {
            Log.e("NoWork","Unable to save image to Gallery $e")
            Result.failure()
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
     * function to call API, retrieve list of all contacts
     * and store individual contact
     */
    fun connectApiAndStore() {

        val BASE_URL = "https://flobooks.in/api/"

        Log.d("entering", "connecting")

        //retrofit initialization
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }

        val contactsApiService: RestApi? = retrofit?.create(RestApi::class.java)
        val call: Call<ContactList>? = contactsApiService?.getAllContactDetails()
        call?.enqueue(object : Callback<ContactList> {
            override fun onResponse(
                call: Call<ContactList>,
                response: Response<ContactList>
            ) {
                Log.d("AllContacts",response.message())
                val contactList: ContactList = response.body()
                if(contactList!= null) {
                    Log.d("AllContacts", contactList.allContacts.size.toString())
                    GlobalScope.launch() {

                        for (x in contactList.allContacts) {
                            val builder = Data.Builder()
                            builder.putString("name", x.name)
                            builder.putString("phone", x.phoneNo)
                            val saveRequest = OneTimeWorkRequestBuilder<SaveWorker>()
                            .setInputData(builder.build())
                            .build()
                            val workManager: WorkManager = WorkManager.getInstance(applicationContext)
                            workManager.enqueue(saveRequest)
                            Thread.sleep(50)
                        }

                    }.invokeOnCompletion {

                    }
                }else{
                    Log.d("AllContacts", "contacts null")
                }

            }
            override fun onFailure(
                call: Call<ContactList>,
                throwable: Throwable
            ) {
                val TAG = "AllContacts"
                Log.e(TAG, throwable.toString())

            }
        })

    }

}