package io.github.orioneee

interface KVDebbugable{
    fun getAll(): Map<String, Any?>
    fun edit(key: String, value: String)
    fun remove(key: String)
    val name: String

}