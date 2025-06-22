package com.oriooneee.ktorin.domain

enum class TimeFilter(val duration: Long, val text: String) {
    ALL(Long.MAX_VALUE, "Any time"),
    LESS_THAN_1_MINUTE(60 * 1000, "Less than 1 minute"),
    LESS_THAN_5_MINUTES(5 * 60 * 1000, "Less than 5 minutes"),
    LESS_THAN_15_MINUTES(15 * 60 * 1000, "Less than 15 minutes"),
}