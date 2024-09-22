package com.auth_service.domain.application

import com.auth_service.domain.application.dto.request.SignInRequestDTO
import com.auth_service.domain.application.dto.request.SignUpRequestDTO
import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.repository.UserRepository
import com.auth_service.global.auth.jwt.TokenResponseDTO
import com.auth_service.global.util.RedisUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.CannotAcquireLockException
import java.time.Duration
import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class UserServiceConcurrencyTest {

    @Autowired
    private lateinit var userManageService: UserManageService

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `Test Concurrency in createLocalUser() with 5 Threads`() {
        // given
        val startTime = System.nanoTime()
        val signUpRequestDTO = SignUpRequestDTO(
            email = "qkrdbsgh96@gmail.com",
            password = "123123",
            name = "Yunho Park",
            nickname = "test",
            profileUrl = "test-image.jpg"
        )

        val threads = 5
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        repeat(threads) {
            executorService.execute {
                try {
                    val result = userManageService.createLocalUser(signUpRequestDTO)
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
        assertTrue(failureResults.all { it is CannotAcquireLockException }, "발생한 예외는 모두 CannotAcquireLockException 이어야 합니다.")
        assertNotNull(userRepository.findById(successResults.first()), "회원가입이 완료된 회원 조회에 성공해야 합니다.")

        println("테스트 실행 시간 : ${duration.toMillis()} 밀리초")
    }

    @Test
    fun `Test Concurrency in deleteLocalUser() with 2 Threads`() {
        // given
        val startTime = System.nanoTime()
        val userId = 1L
        val token = "token"

        val threads = 2
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successCount = AtomicInteger(0)
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        repeat(threads) {
            executorService.execute {
                try {
                    userManageService.deleteLocalUser(userId, token)
                    successCount.getAndIncrement()
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
        assertEquals(1, successCount.get(), "1개의 유저 삭제 처리만 성공해야 합니다.")
        assertEquals(threads - 1, failureResults.size, "${threads - 1}개의 유저 삭제 처리는 실패해야 합니다.")
        assertEquals(userRepository.findById(userId), Optional.empty<User>(), "삭제 처리가 완료된 유저 조회 결과는 Optional.empty() 이어야 합니다.")

        println("테스트 실행 시간 : ${duration.toMillis()} 밀리초")
    }

    @Test
    fun `Test Concurrency in signInByLocalUser() with 2 Threads`() {
        // given
        val startTime = System.nanoTime()
        val signInRequestDTO = SignInRequestDTO(
            email = "tester@gmail.com",
            password = "123123"
        )

        val threads = 2
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successResults = CopyOnWriteArrayList<TokenResponseDTO>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        repeat(threads) {
            executorService.execute {
                try {
                    val result = authService.signInByLocalUser(signInRequestDTO)
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
        assertEquals(1, successResults.size, "1개의 로그인만 성공해야 합니다.")
        assertEquals(threads - 1, failureResults.size, "${threads - 1}개의 로그인은 실패해야 합니다.")
        assertEquals(
            RedisUtils.getValue(successResults.first().accessToken),
            successResults.first().refreshToken,
            "로그인 후 성공적으로 Redis 에 refresh token 이 저장되어야 합니다."
        )

        println("테스트 실행 시간 : ${duration.toMillis()} 밀리초")
    }
}