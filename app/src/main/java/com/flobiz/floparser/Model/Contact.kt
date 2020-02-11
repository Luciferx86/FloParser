package com.flobiz.floparser.Model

import com.google.gson.annotations.SerializedName

class Contact {
    @SerializedName("name")
    var name: String? = ""

    @SerializedName("mobile_number")
    var phoneNo: String? = ""

}