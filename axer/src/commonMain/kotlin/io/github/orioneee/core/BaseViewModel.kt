package io.github.orioneee.core

import androidx.lifecycle.ViewModel

internal open class BaseViewModel : ViewModel() {

    fun showMessage(message: String) {
        println("Message: $message")
    }


}