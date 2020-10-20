package io.bartendr.barback.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMailMessage
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import javax.mail.internet.MimeMessage

@Component
class EmailServiceImpl {

    @Autowired
    lateinit var javaMailSender: JavaMailSender

    fun sendSimpleMessage(
            to: String,
            subj: String,
            text: String
    ) {
        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        message.setHeader("Content-Type", "text/html; charset=utf-8")
        helper.setTo(to)
        helper.setSubject(subj)
        message.setContent(text, "text/html; charset=utf-8")
        javaMailSender.send(message)
    }

}