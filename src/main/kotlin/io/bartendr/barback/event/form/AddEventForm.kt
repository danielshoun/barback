package io.bartendr.barback.event.form

import java.util.*

class AddEventForm (
        val name: String,
        val value: Int,
        val startTime: Date,
        val closeTime: Date,
        val categoryId: Long
        )