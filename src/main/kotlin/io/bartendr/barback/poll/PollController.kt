package io.bartendr.barback.poll

import io.bartendr.barback.poll.form.CreatePollForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PollController {

    @Autowired
    lateinit var pollService: PollService

    @PostMapping("/api/v1/organization/{organizationId}/poll/create")
    fun createPoll(
            @PathVariable(name = "organizationId") organizationId: Long,
            @RequestBody createPollForm: CreatePollForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return pollService.createPoll(organizationId, createPollForm, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/poll/{pollId}/submit")
    fun submitPoll(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "pollId") pollId: Long,
            @RequestParam(name = "choiceId") choiceId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return pollService.submitPoll(organizationId, pollId, choiceId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/poll/current")
    fun getCurrentPolls(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Poll>> {
        return pollService.getCurrentPolls(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/poll/past")
    fun getPastPolls(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Poll>> {
        return pollService.getPastPolls(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/poll/{pollId}/choices")
    fun getPollChoices(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "pollId") pollId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<PollChoice>> {
        return pollService.getPollChoices(organizationId, pollId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/poll/{pollId}/results")
    fun getPollResults(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "pollId") pollId: Long,
            @CookieValue(name = "session") session: String
    ): ResponseEntity<MutableList<ChoiceResult>> {
        return pollService.getPollResults(organizationId, pollId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/poll/{pollId}/check")
    fun checkVote(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "pollId") pollId: Long,
            @RequestParam(name = "hash") voteHash: String,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<PollChoice> {
        return pollService.checkVote(organizationId, pollId, voteHash, session)
    }

}