package io.bartendr.barback.user

import io.bartendr.barback.organization.Organization
import io.bartendr.barback.user.form.LoginForm
import io.bartendr.barback.user.form.RegForm
import io.bartendr.barback.user.form.ResetForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
class UserController {

    @Autowired
    lateinit var userService: UserService

    @PostMapping("/api/v1/user/register")
    fun register(
            @RequestBody regForm: RegForm,
            response: HttpServletResponse
    ): ResponseEntity<String> {
        return userService.register(regForm)
    }

    @PostMapping("/api/v1/user/login")
    fun login(
            @RequestBody loginForm: LoginForm,
            response: HttpServletResponse
    ): ResponseEntity<String> {
        val responseEntity = userService.login(loginForm)
        return if(responseEntity.statusCode == HttpStatus.ACCEPTED) {
            val sessionCookie = Cookie("session", responseEntity.body)
            sessionCookie.path = "/"
            if(loginForm.stayLoggedIn) {
                sessionCookie.maxAge = 60*60*24*365
            }
            else {
                sessionCookie.maxAge = -1
            }
            response.addCookie(sessionCookie)
            responseEntity
        }
        else {
            responseEntity
        }
    }

    @PostMapping("/api/v1/user/logout")
    fun logout(
            @CookieValue(value = "session") session: String,
            response: HttpServletResponse
    ): ResponseEntity<String> {
        val responseEntity = userService.logout(session)
        return if(responseEntity.body == "LOGOUT") {
            val sessionCookie = Cookie("session", null)
            sessionCookie.path = "/"
            sessionCookie.maxAge = 0
            response.addCookie(sessionCookie)
            responseEntity
        }
        else {
            responseEntity
        }
    }

    @GetMapping("/api/v1/user/auth")
    fun authCheck(
            @CookieValue(value = "session") session: String
    ): ResponseEntity<User> {
        return userService.authCheck(session)
    }

    @PostMapping("/api/v1/user/verify")
    fun verifyEmail(
            @RequestParam(name = "token") token: String
    ): ResponseEntity<String> {
        return userService.verifyEmail(token)
    }


    @PostMapping("/api/v1/user/forgot")
    fun forgotPassword(
            @RequestParam(name = "email") emailAddress: String
    ): ResponseEntity<String> {
        return userService.forgotPassword(emailAddress)
    }

    @PostMapping("/api/v1/user/reset")
    fun resetPassword(
            @RequestParam(name = "token") token: String,
            @RequestBody resetForm: ResetForm
    ): ResponseEntity<String> {
        return userService.resetPassword(token, resetForm.newPassword)
    }

    @GetMapping("/api/v1/user/{userId}")
    fun getUser(
            @PathVariable(name = "userId") userId: Long
    ): ResponseEntity<User> {
        return userService.getUser(userId)
    }

    @GetMapping("/api/v1/user/self")
    fun getSelf(
            @CookieValue(value = "session") session: String
    ): ResponseEntity<User> {
        return userService.getSelf(session)
    }

    @GetMapping("/api/v1/user/organizations")
    fun getOwnOrganizations(
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Organization>> {
        return userService.getOwnOrganizations(session)
    }

}