package io.github.orioneee.presentation.navigation

internal enum class Routes(val route: String) {
    REQUESTS_FLOW("requests_flow"),
    EXCEPTIONS_FLOW("exceptions_flow"),
    DATABASE_FLOW("database_flow"),

    REQUESTS_LIST("requests_list"),
    REQUEST_DETAIL("request_detail"),
    SANDBOX("sandbox"),

    EXCEPTIONS_LIST("exceptions_list"),
    EXCEPTION_DETAIL("exception_detail"),

    TABLES_LIST("tables_list"),
    TABLE_DETAILS("table_details"),
    RAW_QUERY("raw_query"),
}