package com.flobiz.floparser.workers

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.OperationApplicationException
import android.net.Uri
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class SaveWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {

        return try {

            Log.d("SaveWorker","started")


            val name = inputData.getString("name")
            val phone = inputData.getString("phone")
//            makeStatusNotification("Saving Contacts", applicationContext, "WorkRequest Starting")

            addContact(name,phone)


            val x = Data.Builder()
            Result.success(x.build())

        } catch (e: Exception) {
            Log.e("NoWork","Unable to save image to Gallery $e")
            Result.failure()
        }

    }

    /**
     * function to access the android service to store a new contact
     * @param name: name of the contact
     * @param number: number of contact
     */

    private fun addContact(name:String?, number: String?) {

        var finalName = ""
        val finalNumber = number



        if(name?.isEmpty() == true){
            val tsLong = System.currentTimeMillis() / 1000
            val ts = tsLong.toString()
            finalName = "NoName$ts"
        }else{
            finalName = name.toString()
            finalName = finalName.substring(1,(finalName.length - 2))
        }

        val ops = ArrayList<ContentProviderOperation>()

        val rawContactID: Int = ops.size
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
// to insert display name in the table ContactsContract.Data
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, finalName)
                .build()
        )
// to insert Mobile Number in the table ContactsContract.Data
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, finalNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()

            //
        )
        try { // Executing all the insert operations as a single database transaction

            Log.d(
                "AddingContact", "Name: $finalName Number: $finalNumber"
            )
            applicationContext.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            Thread.sleep(50)
            Log.d("Contact Saved","Saved")
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e: OperationApplicationException) {
            e.printStackTrace()
        }
    }

    /**
     * function to check whether contact list is empty or not
     */


}