package io.bartendr.barback.poll

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PollRepository : JpaRepository<Poll, Long> {

    fun findAllByStartTimeBeforeAndEndTimeAfter(startTime: Date, endTime: Date): List<Poll>
    fun findAllByEndTimeBefore(endTime: Date): List<Poll>

}