package com.hxg.financialchartdemo.util

import java.lang.ref.WeakReference
import java.util.*

class WeakArrayList<E> {

    internal var data: ArrayList<WeakReference<E>>

    constructor() {
        data = ArrayList()
    }

    constructor(capacity: Int) {
        data = ArrayList(capacity)
    }

    fun Contains(o: E): Boolean {
        return data.any { it.get() == o }
    }

    fun Remove(o: E): Boolean {
        val oldSize = data.size
        //this code looks nice but it allocates a new ArrayList each time
//        data = data.filter { it.get() != o } as ArrayList<WeakReference<E>>
        var size = oldSize
        var i = 0
        while (i < size) {
            val ref = data[i]
            if (ref.get() == o) {
                data.removeAt(i)
                //since removed..both and size is offset -1...
                i--
                size--
            }
            i++
        }
        //size new size is smaller, remove success
        return data.size < oldSize
    }

    //clone does not return com.xutil.WeakArrayList but a non-weak ArrayList
    fun Clone(): ArrayList<E> {
        val size = Size()

        val tmp = ArrayList<E>(size)

        for (i in 0 until size) {
            val ref = data[i]
            val o = ref.get()
            if (o != null) {
                tmp.add(o)
            }
        }

        return tmp
    }

    fun Add(o: E): Boolean {
        data.add(WeakReference(o))
        return true
    }

    //may return null..due to weakReference
    fun Get(index: Int): E? {
        return data[index].get()
    }

    fun Add(index: Int, o: E) {
        data.add(index, WeakReference(o))
    }

    fun Remove(index: Int): E? {
        return data.removeAt(index).get()
    }

    fun Clear() {
        data.clear()
    }

    //note this is very slow since each call calls gcReleased!
    fun Size(): Int {
        gcReleased()
        return data.size
    }

    private fun gcReleased() {
        var size = data.size
        var i = 0
        while (i < size) {
            val ref = data[i]
            if (ref.get() == null) {
                data.removeAt(i)
                //since removed..both and size is offset -1...
                i--
                size--
            }
            i++
        }
    }
}

