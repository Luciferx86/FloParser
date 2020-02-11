package com.flobiz.floparser.Model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ContactList(response: Array<Contact>) {
    @SerializedName("response")
    @Expose
//    var contacts: List<Contact> = contacts
    var allContacts : Array<Contact> = response

}
