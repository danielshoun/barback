package io.bartendr.barback.poll

import org.springframework.data.jpa.repository.JpaRepository

interface PollChoiceRepository: JpaRepository<PollChoice, Long> {

    fun findAllByPoll(poll: Poll): List<PollChoice>
    fun findByHashes(hash: String): PollChoice

}