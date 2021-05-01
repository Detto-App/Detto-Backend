package com.dettoapp.data

import java.util.*

class CircularList<E>(maxSize: Int = 4) : LinkedList<E>() {
    @Suppress("PrivatePropertyName")
    private val MAX_SIZE = maxSize
    override fun add(element: E): Boolean {
        super.addLast(element)
        if (this.size > MAX_SIZE)
            this.remove()
        return true
    }

    fun getList(isReversing: Boolean = false): List<E> {
        val arrayList = ArrayList<E>(this)
        if (isReversing)
            arrayList.reverse()
        return arrayList
    }
}