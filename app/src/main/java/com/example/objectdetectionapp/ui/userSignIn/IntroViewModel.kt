package com.example.objectdetectionapp.ui.userSignIn

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.models.User
import com.example.objectdetectionapp.data.repository.SignInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IntroViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "IntroViewModel"
    private val firebaseService = FirebaseServiceImpl()
    private val signInRepository = SignInRepository(application, firebaseService)

    // Define sign-in states
    sealed class SignInState {
        data object Idle : SignInState()
        data object Loading : SignInState()
        data object Success : SignInState()
        data object AlreadySignedIn : SignInState()
        data class Error(val message: String) : SignInState()
    }

    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    // Controls the initial loading screen
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Controls whether the initial DataStore check is complete
    private val _initialCheckComplete = MutableStateFlow(false)
    val initialCheckComplete: StateFlow<Boolean> = _initialCheckComplete.asStateFlow()

    init {
        // Start checking sign-in status immediately
        checkIfSignedIn()
    }

    private fun checkIfSignedIn() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking if user is signed in...")

                // Keep loading state true throughout the check
                _isLoading.value = true

                // Fetch sign-in status from DataStore
                val hasSignedIn = signInRepository.hasSignedIn.first()

                if (hasSignedIn) {
                    // If already signed in, get saved user details
                    val savedUser = signInRepository.getUserDetails()
                    savedUser?.let {
                        _username.value = it.username
                        _email.value = it.email
                        Log.d(TAG, "User already signed in: ${it.username}")
                    }

                    // Set state to AlreadySignedIn - this will trigger navigation
                    _signInState.value = SignInState.AlreadySignedIn

                    // Keep loading true for already signed in users to prevent UI flash
                    // The UI will handle navigation directly based on AlreadySignedIn state
                } else {
                    _signInState.value = SignInState.Idle
                    Log.d(TAG, "User not signed in")

                    // Only turn off loading for users who need to sign in
                    _isLoading.value = false
                }

                // Mark initial check as complete - this helps prevent UI flash
                _initialCheckComplete.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error checking sign-in status: ${e.message}")
                _signInState.value = SignInState.Error("Failed to check sign-in status: ${e.message}")
                _isLoading.value = false
                _initialCheckComplete.value = true
            }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _username.value = newUsername
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun saveUserDetails() {
        if (username.value.isBlank() || email.value.isBlank()) {
            _signInState.value = SignInState.Error("Username and email cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _signInState.value = SignInState.Loading
                Log.d(TAG, "Saving user details: ${username.value}, ${email.value}")

                val user = User(username.value, email.value)
                signInRepository.saveUserDetails(user)

                _signInState.value = SignInState.Success
                Log.d(TAG, "User details saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user details: ${e.message}")
                _signInState.value = SignInState.Error("Failed to save user details: ${e.message}")
            }
        }
    }
}