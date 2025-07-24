package io.github.orioneee.core

import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    fun showMessage(message: String) {
        println("Message: $message")
    }


}