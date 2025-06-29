package com.oriooneee.axer.room

import androidx.sqlite.driver.bundled.BundledSQLiteDriver

class AxerBundledSQLiteDriver private constructor() {
    companion object {
        fun getInstance() = BundledSQLiteDriver()
    }
}