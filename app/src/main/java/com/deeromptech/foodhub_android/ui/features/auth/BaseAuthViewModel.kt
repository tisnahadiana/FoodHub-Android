package com.deeromptech.foodhub_android.ui.features.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeromptech.foodhub_android.data.FoodApi
import com.deeromptech.foodhub_android.data.auth.GoogleAuthUiProvider
import com.deeromptech.foodhub_android.data.models.OAuthRequest
import com.deeromptech.foodhub_android.data.remote.ApiResponse
import com.deeromptech.foodhub_android.data.remote.safeApiCall

import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel (open val foodApi: FoodApi) : ViewModel() {
    var error: String = ""
    var errorDescription = ""
    private val googleAuthUiProvider = GoogleAuthUiProvider()
    private lateinit var callbackManager: CallbackManager


    abstract fun loading()
    abstract fun onGoogleError(msg: String)
    abstract fun onFacebookError(msg: String)
    abstract fun onSocialLoginSuccess(token: String)

    fun onFacebookClicked(context: ComponentActivity) {
        initiateFacebookLogin(context)
    }

    fun onGoogleClicked(context: ComponentActivity) {
        initiateGoogleLogin(context)
    }

    protected fun initiateGoogleLogin(context: ComponentActivity) {
        viewModelScope.launch {
            loading()
            try {
                val response = googleAuthUiProvider.signIn(
                    context, CredentialManager.create(context)
                )
                fetchFoodAppToken(response.token, "google") {
                    onGoogleError(it)
                }
            } catch (e: Throwable) {
                onGoogleError(e.message.toString())
            }
        }
    }

    private fun fetchFoodAppToken(token: String, provider: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val request = OAuthRequest(
                token = token, provider = provider
            )
            val res = safeApiCall { foodApi.oAuth(request) }
            when (res) {
                is ApiResponse.Success -> {
                    onSocialLoginSuccess(res.data.token)
                }

                else -> {
                    val error = (res as? ApiResponse.Error)?.code
                    if (error != null) {
                        when (error) {
                            401 -> onError("Invalid Token")
                            500 -> onError("Server Error")
                            404 -> onError("Not Found")
                            else -> onError("Failed")
                        }
                    } else {
                        onError("Failed")
                    }
                }
            }
        }
    }

    protected fun initiateFacebookLogin(context: ComponentActivity) {
        loading()
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    fetchFoodAppToken(loginResult.accessToken.token, "facebook") {
                        onFacebookError(it)
                    }
                }

                override fun onCancel() {
                    onFacebookError("Cancelled")
                }

                override fun onError(exception: FacebookException) {
                    onFacebookError("Failed: ${exception.message}")
                }
            })

        LoginManager.getInstance().logInWithReadPermissions(
            context,
            callbackManager,
            listOf("public_profile", "email"),
        )
    }
}