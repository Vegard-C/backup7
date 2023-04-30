package com.backup7

class Result<T> private constructor (
    private val error: String?,
    private val value: T?
    ) {
    constructor(error: String) : this(error, null)
    constructor(value: T): this(null, value)
    fun value(): T {
        if (error != null) throw IllegalStateException("value called on error")
        return value ?: throw IllegalStateException("value is null")
    }
    fun error(): String {
        if (error == null) throw IllegalStateException("error called on value")
        return error
    }
    fun failed(): Boolean = error != null
}