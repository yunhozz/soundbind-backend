package com.auth_service.domain.application

import com.auth_service.global.util.RedisUtils
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Random

@Service
class MailService(private val mailSender: JavaMailSender) {

    @Async
    fun sendVerifyingEmail(email: String) {
        val message = mailSender.createMimeMessage()
        val code = createRandomCode()
        val content = makeVerifyingContentWithCode(code)

        MimeMessageHelper(message, false, "UTF-8").apply {
            setTo(email)
            setSubject("[SOUND BIND] 회원가입 인증 메일입니다.")
            setText(content, true)
        }
        mailSender.send(message)
        RedisUtils.saveValue("verify:$email", code, Duration.ofDays(1))
    }

    companion object {
        private fun createRandomCode(): String {
            val code = StringBuilder()
            val random = Random(System.currentTimeMillis())
            for (i in 0 until 8) {
                val index = random.nextInt(3)
                when (index) {
                    0 -> code.append((random.nextInt(26) + 65).toChar()) // A ~ Z
                    1 -> code.append((random.nextInt(26) + 97).toChar()) // a ~ z
                    2 -> code.append(random.nextInt(10)) // 0 ~ 9
                }
            }
            return code.toString()
        }

        private fun makeVerifyingContentWithCode(code: String): String =
            StringBuilder().apply {
                append("<html><body style='background-color: #000000 !important; margin: 0 auto; max-width: 600px; word-break: break-all; padding-top: 50px; color: #ffffff;'>")
                append("<img class='logo' src='cid:image'>")
                append("<h1 style='padding-top: 50px; font-size: 30px;'>이메일 주소 인증</h1>")
                append("<p style='padding-top: 20px; font-size: 18px; opacity: 0.6; line-height: 30px; font-weight: 400;'>안녕하세요? SoundBind 관리자 입니다.<br />")
                append("SoundBind 서비스 사용을 위해 회원가입시 고객님께서 입력하신 이메일 주소의 인증이 필요합니다.<br />")
                append("하단의 인증 번호로 이메일 인증을 완료하시면, 정상적으로 SoundBind 서비스를 이용하실 수 있습니다.<br />")
                append("항상 최선의 노력을 다하는 SoundBind가 되겠습니다.<br />")
                append("감사합니다.</p>")
                append("<div class='code-box' style='margin-top: 50px; padding-top: 20px; color: #000000; padding-bottom: 20px; font-size: 25px; text-align: center; background-color: #f4f4f4; border-radius: 10px;'>")
                append(code)
                append("</div></body></html>")
            }.toString()
    }
}