package io.bartendr.barback.poll

import io.bartendr.barback.organization.OrganizationRepository
import io.bartendr.barback.poll.form.CreatePollForm
import io.bartendr.barback.role.RoleRepository
import io.bartendr.barback.user.UserRepository
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class PollService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var pollRepository: PollRepository

    @Autowired
    lateinit var pollChoiceRepository: PollChoiceRepository

    fun createPoll(
            organizationId: Long,
            createPollForm: CreatePollForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManagePolls")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val newPoll = Poll(
                question = createPollForm.question,
                startTime = createPollForm.startTime,
                endTime = createPollForm.endTime
        )

        pollRepository.save(newPoll)

        for(choice in createPollForm.choices) {
            val newPollChoice = PollChoice(
                    poll = newPoll,
                    text = choice
            )
            pollChoiceRepository.save(newPollChoice)
        }

        return ResponseEntity(HttpStatus.OK)
    }

    fun submitPoll(
            organizationId: Long,
            pollId: Long,
            choiceId: Long,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val poll = pollRepository.findByIdOrNull(pollId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val choice = pollChoiceRepository.findByIdOrNull(choiceId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        if(choice.poll != poll) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(poll.usersVotedIn.contains(requester)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(poll.endTime < Date()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val voteHash = UUID.randomUUID().toString()
        try {
            choice.hashes.add(voteHash)
            pollChoiceRepository.save(choice)
            poll.usersVotedIn.add(requester)
            pollRepository.save(poll)
        } catch (e: ConstraintViolationException) {
            print("Attempted to add duplicate hash $voteHash")
        }


        return ResponseEntity(voteHash, HttpStatus.OK)
    }

    fun getCurrentPolls(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<Poll>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val currentTime = Date()

        val currentPolls = pollRepository.findAllByStartTimeBeforeAndEndTimeAfter(currentTime, currentTime)

        return ResponseEntity(currentPolls, HttpStatus.OK)
    }

    fun getPastPolls(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<Poll>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val currentTime = Date()

        val pastPolls = pollRepository.findAllByEndTimeBefore(currentTime)

        return ResponseEntity(pastPolls, HttpStatus.OK)
    }

    fun getPollChoices(
            organizationId: Long,
            pollId: Long,
            session: String
    ): ResponseEntity<List<PollChoice>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val poll = pollRepository.findByIdOrNull(pollId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val choices = pollChoiceRepository.findAllByPoll(poll)

        return ResponseEntity(choices, HttpStatus.OK)
    }

    fun getPollResults(
            organizationId: Long,
            pollId: Long,
            session: String
    ): ResponseEntity<MutableList<ChoiceResult>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val poll = pollRepository.findByIdOrNull(pollId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val choices = pollChoiceRepository.findAllByPoll(poll)

        val results: MutableList<ChoiceResult> = mutableListOf()

        for(choice in choices) {
            results.add(ChoiceResult(choice.poll, choice.text, choice.hashes.size))
        }

        results.sortByDescending {it.total}

        return ResponseEntity(results, HttpStatus.OK)
    }

    fun checkVote(
            organizationId: Long,
            pollId: Long,
            voteHash: String,
            session: String
    ): ResponseEntity<PollChoice> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val poll = pollRepository.findByIdOrNull(pollId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val choice = pollChoiceRepository.findByHashes(voteHash)

        return ResponseEntity(choice, HttpStatus.OK)
    }

}