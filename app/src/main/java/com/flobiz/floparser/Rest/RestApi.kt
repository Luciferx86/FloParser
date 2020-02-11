package com.flobiz.floparser.Rest

import com.flobiz.floparser.Model.ContactList
import retrofit2.Call
import retrofit2.http.GET

interface RestApi {

    @GET("admin/users/all_mobile_numbers")
    fun getAllContactDetails(): Call<ContactList>
}