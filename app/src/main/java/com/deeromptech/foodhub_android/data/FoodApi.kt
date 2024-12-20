package com.deeromptech.foodhub_android.data

import com.deeromptech.foodhub_android.data.models.AuthResponse
import com.deeromptech.foodhub_android.data.models.OAuthRequest
import com.deeromptech.foodhub_android.data.models.SignInRequest
import com.deeromptech.foodhub_android.data.models.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FoodApi {

    @GET("food")
    suspend fun getFood(): List<String>

    @POST("/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("/auth/oauth")
    suspend fun oAuth(@Body request: OAuthRequest): Response<AuthResponse>

}