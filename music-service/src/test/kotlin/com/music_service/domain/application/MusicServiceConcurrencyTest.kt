package com.music_service.domain.application

import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.util.RedisUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

@SpringBootTest
class MusicServiceConcurrencyTest {

    @Autowired
    private lateinit var musicService: MusicService

    @Autowired
    private lateinit var musicRepository: MusicRepository

    @Test
    fun `Test Concurrency in uploadMusic() with 1000 Threads`() {
        // given
        val startTime = System.currentTimeMillis()

        val musicFile = MockMultipartFile("file", "music-file.mp4", "audio/mp4", "This is Music".toByteArray())
        val imageFile = MockMultipartFile("file", "image-file.jpg", "image/jpeg", "This is Image".toByteArray())
        val musicCreateDTO = MusicCreateDTO(
            title = "Test Music",
            genres = setOf("POP", "ROCK"),
            musicFile = musicFile,
            imageFile = imageFile
        )

        val threads = 1000
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        (1..threads).toList()
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

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        repeat(threads) {
            executorService.execute {
                val userId = ThreadLocalRandom.current().nextLong(1, threads.toLong())
                try {
                    val result = musicService.uploadMusic(userId, musicCreateDTO)
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

        val endTime = System.currentTimeMillis()

        // then
        println("테스트 실행 시간 : ${endTime - startTime} 밀리초")

        assertEquals(threads, successResults.size, "$threads 개의 음원 업로드에 모두 성공해야 합니다.")
        assertEquals(0, failureResults.size, "음원 업로드에 실패한 thread 는 없어야 합니다.")
    }
}