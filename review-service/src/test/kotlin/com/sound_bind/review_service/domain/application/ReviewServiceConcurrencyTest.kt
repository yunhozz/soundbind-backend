package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.repository.ReviewLikesRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.global.exception.ReviewServiceException
import com.sound_bind.review_service.global.util.RedisUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class ReviewServiceConcurrencyTest {

    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Autowired
    private lateinit var reviewLikesRepository: ReviewLikesRepository

    companion object {
        private const val THREADS_5 = 5
        private const val THREADS_50 = 50
        private const val THREADS_100 = 100
        private const val THREADS_500 = 500
        private const val THREADS_1000 = 1000
    }

    @BeforeEach
    fun initUserInfoOnRedis() {
        (1..THREADS_1000).toList()
            .map { userId ->
                mapOf(
                    "email" to "tester$userId@gmail.com",
                    "userId" to userId,
                    "nickname" to "tester$userId",
                    "profileUrl" to "test-image-$userId.jpg"
                )
            }.forEach { userInfo ->
                RedisUtils.saveJson("user:${userInfo["userId"]}", userInfo, Duration.ofSeconds(30))
            }
    }

    @AfterEach
    fun cleanupRedis() {
        (1..THREADS_1000).toList()
            .forEach { userId ->
                RedisUtils.deleteValue("user:$userId")
            }
    }

    @Test
    fun `Test Concurrency in createReview() with 5 Threads`() {
        // given
        val reviewerId = 999L
        val musicId = 1L

        val executorService = Executors.newFixedThreadPool(THREADS_5)
        val latch = CountDownLatch(THREADS_5)

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        val startTime = System.currentTimeMillis()

        repeat(THREADS_5) {
            executorService.execute {
                try {
                    val result = reviewService.createReview(
                        musicId,
                        reviewerId,
                        ReviewCreateDTO(message = "This is Test Review", score = 5.0)
                    )
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

        // then
        println("테스트 실행 시간 : ${System.currentTimeMillis() - startTime} 밀리초")

        assertEquals(1, successResults.size, "$THREADS_5 개 중 1개의 리뷰 생성만 성공해야 합니다.")
        assertEquals(THREADS_5 - 1, failureResults.size, "${THREADS_5 - 1} 개의 thread 들은 리뷰 생성에 실패해야 합니다.")
        assertTrue(
            failureResults.all { it is ReviewServiceException.ReviewAlreadyExistException },
            "발생한 예외는 모두 ReviewAlreadyExistException 이어야 합니다.")
    }

    @Test
    fun `Test Concurrency in updateReviewMessageAndScore() with 5 Threads`() {
        // given
        val reviewerId = 999L
        val musicId = 1L

        val review = Review.create(musicId, reviewerId, "Tester", null, "This is Test Review", 5.0)
        reviewRepository.save(review)

        val executorService = Executors.newFixedThreadPool(THREADS_5)
        val latch = CountDownLatch(THREADS_5)

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        val updatedMessage = "Review Updated!!"
        val updatedScore = 1.0

        // when
        val startTime = System.currentTimeMillis()

        repeat(THREADS_5) {
            executorService.execute {
                try {
                    val result = reviewService.updateReviewMessageAndScore(
                        musicId,
                        reviewerId,
                        ReviewUpdateDTO(updatedMessage, updatedScore)
                    )
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

        // then
        println("테스트 실행 시간 : ${System.currentTimeMillis() - startTime} 밀리초")

        successResults.forEach { println(it) }
        failureResults.forEach { println(it) }

        assertEquals(1, successResults.size, "$THREADS_5 개 중 1개의 리뷰 업데이트만 성공해야 합니다.")
        assertEquals(THREADS_5 - 1, failureResults.size, "${THREADS_5 - 1} 개의 thread 들은 리뷰 업데이트에 실패해야 합니다.")
        assertTrue(
            successResults.all {
                val updatedReview = reviewRepository.findById(it).get()
                updatedReview.message == updatedMessage && updatedReview.score == updatedScore
            },
            "리뷰 업데이트가 올바르게 실행되어야 합니다."
        )
    }

    @Test
    fun `Test Concurrency in changeLikesFlag() with 1000 Threads`() {
        // given
        val reviewerId = 999L
        val musicId = 1L

        val review = Review.create(musicId, reviewerId, "Tester", null, "This is Test Review", 5.0)
        reviewRepository.save(review)

        val executorService = Executors.newFixedThreadPool(THREADS_1000)
        val latch = CountDownLatch(THREADS_1000)

        val successResults = AtomicInteger(0)
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        val startTime = System.currentTimeMillis()

        repeat(THREADS_1000) {
            executorService.execute {
                try {
                    val randomUserId = ThreadLocalRandom.current().nextLong(1, THREADS_1000.toLong())
                    reviewService.changeLikesFlag(review.id!!, randomUserId)
                    successResults.getAndIncrement()

                } catch (e: Exception) {
                    failureResults.add(e)

                } finally {
                    latch.countDown()
                }
            }
        }
        executorService.shutdown()
        latch.await()

        // then
        println("테스트 실행 시간 : ${System.currentTimeMillis() - startTime} 밀리초")

        assertEquals(THREADS_1000, successResults.get(), "$THREADS_1000 개의 리뷰 좋아요 요청에 모두 성공해야 합니다.")
        assertEquals(0, failureResults.size, "리뷰 좋아요에 실패한 thread 는 없어야 합니다.")
        assertEquals(
            reviewRepository.findById(review.id!!).get().likes,
            reviewLikesRepository.findReviewLikesByFlag(true).size,
            "무작위 $THREADS_1000 명의 리뷰 좋아요에 모두 성공해야 합니다."
        )
    }

    @Test
    fun `Test Concurrency in deleteReview() with 5 Threads`() {
        // given
        val reviewerId = 999L
        val musicId = 1L

        val review = Review.create(musicId, reviewerId, "Tester", null, "This is Test Review", 5.0)
        reviewRepository.save(review)

        val executorService = Executors.newFixedThreadPool(THREADS_5)
        val latch = CountDownLatch(THREADS_5)

        val successResults = AtomicInteger(0)
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        val startTime = System.currentTimeMillis()

        repeat(THREADS_5) {
            executorService.execute {
                try {
                    reviewService.deleteReview(review.id!!, reviewerId)
                    successResults.getAndIncrement()

                } catch (e: Exception) {
                    failureResults.add(e)

                } finally {
                    latch.countDown()
                }
            }
        }
        executorService.shutdown()
        latch.await()

        // then
        println("테스트 실행 시간 : ${System.currentTimeMillis() - startTime} 밀리초")

        assertEquals(1, successResults.get(), "$THREADS_5 개의 리뷰 삭제 요청 중 1개만 성공해야 합니다.")
        assertEquals(THREADS_5 - 1, failureResults.size, "${THREADS_5 - 1} 개의 thread 들은 리뷰 삭제에 실패해야 합니다.")
    }
}