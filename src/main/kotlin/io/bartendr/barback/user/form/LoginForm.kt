package io.bartendr.barback.user.form

class LoginForm(
        val emailAddress: String,
        val plainTextPassword: String,
        val stayLoggedIn: Boolean
)