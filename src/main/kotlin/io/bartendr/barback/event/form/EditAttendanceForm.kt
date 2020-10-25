package io.bartendr.barback.event.form

class EditAttendanceForm(
    val attendedIds: List<Long>,
    val notAttendedIds: List<Long>
)