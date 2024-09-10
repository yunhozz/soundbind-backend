package com.auth_service.domain.application

import com.auth_service.domain.application.dto.request.SignUpRequestDTO
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.global.exception.UserManageException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
class UserManageServiceTest {

    @Autowired
    private lateinit var userManageService: UserManageService

    @Autowired
    private lateinit var userProfileRepository: UserProfileRepository

    @Test
    fun `Test Concurrency in createLocalUser() with 1000 Threads`() {
        // given
        val startTime = System.nanoTime()
        val dto = SignUpRequestDTO(
            email = "tester@gmail.com",
            password = "123123",
            name = "Yunho Park",
            nickname = "tester",
            profileUrl = "test-image.jpg"
        )

        val threads = 1000
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        repeat(threads) {
            executorService.execute {
                try {
                    val result = userManageService.createLocalUser(dto)
                    successResults.add(result)
                } catch (e: Exception) {
                    failureResults.add(e)
                } finally {
                    latch.countDown()
                }
            }
        }
        executorService.shutdown()
        latch.await()

        val endTime = System.nanoTime()
        val duration = Duration.ofNanos(endTime - startTime)

        // then
        assertEquals(1, successResults.size, "1개의 회원가입만 성공해야 합니다.")
        assertEquals(threads - 1, failureResults.size, "${threads - 1}개의 회원가입은 실패해야 합니다.")
        assertTrue(failureResults.all { it is UserManageException.EmailDuplicateException }, "발생한 예외는 모두 EmailDuplicateException 이어야 합니다.")
        assertNotNull(userProfileRepository.findWithUserByUserId(successResults.first()), "회원가입이 완료된 회원 조회에 성공해야 합니다.")

        println("테스트 실행 시간 : ${duration.toMillis()} 밀리초")
    }
}