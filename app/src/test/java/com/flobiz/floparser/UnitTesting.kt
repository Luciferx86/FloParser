package com.flobiz.floparser

import com.flobiz.floparser.workers.CsvContactsWorker
import org.junit.Assert
import org.junit.Test

class UnitTesting {

    @Test
    open fun testAddContact(): Unit {
        val testName = "anupamchugh@gmail.com"
        val testNumber = "9999999999"
        val worker: CsvContactsWorker = object : CsvContactsWorker()

        Assert.assertThat(
            String.format("Add Contact validity failed for ", testName, testNumber),
            NotifUtils.checkEmailForValidity(testEmail),
            `is`(true)
        )
    }
}