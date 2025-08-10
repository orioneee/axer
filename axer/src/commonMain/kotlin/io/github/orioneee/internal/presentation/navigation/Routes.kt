package io.github.orioneee.internal.presentation.navigation

internal enum class Routes(val route: String) {
    REQUESTS_FLOW("requests_flow"),
    EXCEPTIONS_FLOW("exceptions_flow"),
    DATABASE_FLOW("database_flow"),

    REQUESTS_LIST("requests_list"),
    REQUEST_DETAIL("request_detail"),
    SANDBOX("sandbox"),

    EXCEPTIONS_LIST("exceptions_list"),
    EXCEPTION_DETAIL("exception_detail"),

    LOG_VIEW("log_view"),

    TABLES_LIST("tables_list"),
    TABLE_DETAILS("table_details"),
    RAW_QUERY("raw_query"),
    ALL_QUERIES("all_queries"),
}