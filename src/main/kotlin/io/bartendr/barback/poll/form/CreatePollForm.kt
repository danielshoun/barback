package io.bartendr.barback.poll.form

import java.util.*

class CreatePollForm(
        val question: String,
        val choices: MutableList<String>,
        val startTime: Date,
        val endTime: Date
)