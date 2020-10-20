package io.bartendr.barback.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class EmailServiceImpl {

    @Autowired
    lateinit var javaMailSender: JavaMailSender

    fun sendSimpleMessage(
            to: String,
            subj: String,
            text: String
    ) {
        var message = SimpleMailMessage()
        message.setFrom("shoundaniel@gmail.com")
        message.setTo(to)
        message.setSubject(subj)
        message.setText(text)
        javaMailSender.send(message)
    }

}