package io.bartendr.barback.user

import io.bartendr.barback.organization.Organization
import io.bartendr.barback.user.form.LoginForm
import io.bartendr.barback.user.form.RegForm
import io.bartendr.barback.util.EmailServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var sessionRepository: SessionRepository

    @Autowired
    lateinit var emailServiceImpl: EmailServiceImpl

    var bCryptPasswordEncoder = BCryptPasswordEncoder()

    fun register(
            regForm: RegForm
    ): ResponseEntity<String> {
        if(userRepository.findByEmailAddress(regForm.emailAddress.toLowerCase()) != null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(regForm.firstName == "") {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(regForm.lastName == "") {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(regForm.emailAddress == "" || !regForm.emailAddress.contains("@")) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(regForm.plainTextPassword == "" || regForm.plainTextPassword.length < 8) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val newUser = User(
                firstName = regForm.firstName,
                lastName = regForm.lastName,
                emailAddress = regForm.emailAddress.toLowerCase(),
                hashedPassword = bCryptPasswordEncoder.encode(regForm.plainTextPassword),
                dateOfBirth = regForm.dateOfBirth
        )

        if(userRepository.findAll().isEmpty()) {
            newUser.isWebsiteAdmin = true
        }

        userRepository.save(newUser)

        emailServiceImpl.sendSimpleMessage(
                to = newUser.emailAddress,
                subj = "Bartendr: Email Verification",
                text = "<html><body>Please click <a href=\"https://bartendr.io/app/verify/"
                        + newUser.emailVerificationToken + "\">here</a> to verify your email address.</body></html>"
        )

        return ResponseEntity(HttpStatus.CREATED)
    }

    fun login(
            loginForm: LoginForm
    ): ResponseEntity<String> {
        val requester: User = userRepository.findByEmailAddress(loginForm.emailAddress.toLowerCase())?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requester.emailVerified) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        return if(bCryptPasswordEncoder.matches(loginForm.plainTextPassword, requester.hashedPassword)) {
            val session = Session()
            if(requester.sessions.size > 2) {
                val sessionToDelete: Session = requester.sessions[0]
                requester.sessions.remove(sessionToDelete)
                sessionRepository.delete(sessionToDelete)
            }
            requester.sessions.add(session)
            userRepository.save(requester)

            ResponseEntity(session.key, HttpStatus.ACCEPTED)
        }
        else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    fun logout(
            session: String
    ): ResponseEntity<String> {
        val requester: User = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        for(possibleSession in requester.sessions) {
            if(session == possibleSession.key) {
                requester.sessions.remove(possibleSession)
                sessionRepository.delete(possibleSession)
                userRepository.save(requester)
                return ResponseEntity("LOGOUT", HttpStatus.OK)
            }
        }

        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    fun authCheck(
            session: String
    ): ResponseEntity<User> {
        val requester: User = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.NOT_FOUND)

        return ResponseEntity(requester, HttpStatus.OK)
    }

    fun verifyEmail(
            token: String
    ): ResponseEntity<String> {
        val requester: User = userRepository.findByEmailVerificationToken(token)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)
        requester.emailVerified = true
        userRepository.save(requester)
        return ResponseEntity(HttpStatus.OK)
    }

    fun forgotPassword(
            emailAddress: String
    ): ResponseEntity<String> {
        val requester: User = userRepository.findByEmailAddress(emailAddress)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)
        requester.forgottenPassword = true
        requester.forgottenPasswordToken = UUID.randomUUID().toString()
        userRepository.save(requester)

        emailServiceImpl.sendSimpleMessage(
                to = requester.emailAddress,
                subj = "Bartendr: Forgotten Password",
                text = "<html><body>Your password reset token is:<br><br>"
                        + requester.forgottenPasswordToken + "</body></html>"
        )

        return ResponseEntity(HttpStatus.OK)
    }

    fun resetPassword(
            token: String,
            newPassword: String
    ): ResponseEntity<String> {
        val requester: User = userRepository.findByForgottenPasswordToken(token)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)
        requester.hashedPassword = bCryptPasswordEncoder.encode(newPassword)
        requester.forgottenPassword = false
        requester.forgottenPasswordToken = null
        val sessionsToDelete: List<Session> = requester.sessions.toList()
        requester.sessions = mutableListOf()
        sessionRepository.deleteAll(sessionsToDelete)
        userRepository.save(requester)
        return ResponseEntity(HttpStatus.OK)
    }

    fun getUser(
            userId: Long
    ): ResponseEntity<User> {
        val user: User = userRepository.findByIdOrNull(userId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(user, HttpStatus.OK)
    }

    fun getSelf(
            session: String
    ): ResponseEntity<User> {
        val requester: User = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        return ResponseEntity(requester, HttpStatus.OK)
    }

    fun getOwnOrganizations(
            session: String
    ): ResponseEntity<List<Organization>> {
        val requester: User = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        return ResponseEntity(requester.organizations, HttpStatus.OK)
    }

}