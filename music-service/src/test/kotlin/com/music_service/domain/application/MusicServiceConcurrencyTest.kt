package com.music_service.domain.application

import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.application.manager.LockManager
import com.music_service.domain.persistence.repository.MusicLikesRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.util.RedisUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
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

    @Autowired
    private lateinit var musicLikesRepository: MusicLikesRepository

    @Autowired
    private lateinit var lockManager: LockManager

    private val threads = 500

    @BeforeEach
    fun initUserInfoOnRedis() {
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
    }

    @AfterEach
    fun cleanupRedis() {
        (1..threads).toList()
            .forEach { userId ->
                RedisUtils.deleteValue("user:$userId")
            }
    }

    @Test
    fun `Test Concurrency in uploadMusic() with 1000 Users`() {
        // given
        val musicFile = MockMultipartFile("file", "music-file.mp4", "audio/mp4", "This is Music".toByteArray())
        val imageFile = MockMultipartFile("file", "image-file.jpg", "image/jpeg", "This is Image".toByteArray())
        val musicCreateDTO = MusicCreateDTO(
            title = "Test Music",
            genres = setOf("POP", "ROCK"),
            musicFile = musicFile,
            imageFile = imageFile
        )

        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        val startTime = System.currentTimeMillis()

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

        // then
        println("테스트 실행 시간 : ${System.currentTimeMillis() - startTime} 밀리초")

        assertEquals(threads, successResults.size, "$threads 개의 음원 업로드에 모두 성공해야 합니다.")
        assertEquals(0, failureResults.size, "음원 업로드에 실패한 thread 는 없어야 합니다.")
    }

    @Test
    fun `Test Concurrency in updateMusicInformation() with 1000 Users`() {
        // given
        val musicFile = MockMultipartFile("file", "music-file.mp4", "audio/mp4", "This is Music".toByteArray())
        val imageFile = MockMultipartFile("file", "image-file.jpg", "image/jpeg", "This is Image".toByteArray())

        val userIds = (1L..threads).toList()
        for (userId in userIds) {
            val musicCreateDTO = MusicCreateDTO(
                title = "Test Music $userId",
                genres = setOf("POP", "ROCK"),
                musicFile = musicFile,
                imageFile = imageFile
            )
            musicService.uploadMusic(userId, musicCreateDTO)
            Thread.sleep(10)
        }

        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successResults = ConcurrentHashMap<Int, Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        val startTime = System.currentTimeMillis()

        repeat(threads) { index ->
            executorService.execute {
                try {
                    val musicUpdateDTO = MusicUpdateDTO(
                        title = "Music Updated $index",
                        genres = setOf("BALLAD"),
                        imageFile = MockMultipartFile(
                            "file",
                            "update-file-$index.jpg",
                            "image/jpeg",
                            "This is Updated".toByteArray()
                        )
                    )
                    val result = musicService.updateMusicInformation(index.toLong(), musicUpdateDTO)
                    successResults[index] = result

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

        assertEquals(threads, successResults.size, "$threads 개의 음원 업데이트에 모두 성공해야 합니다.")
        assertEquals(0, failureResults.size, "음원 업데이트에 실패한 thread 는 없어야 합니다.")
        assertTrue(musicRepository.findAll().all { it.title.startsWith("Music Updated") }, "모든 음원이 정상적으로 업데이트 되어야 합니다.")
        assertTrue(successResults.keys.toSet().size == threads, "업데이트가 겹치지 않아야 합니다.")
    }

    @Test
    fun `Test Concurrency in changeLikesFlag() with 1000 Users`() {
        // given
        val userId = threads.toLong()

        val musicFile = MockMultipartFile("file", "music-file.mp4", "audio/mp4", "This is Music".toByteArray())
        val imageFile = MockMultipartFile("file", "image-file.jpg", "image/jpeg", "This is Image".toByteArray())
        val musicCreateDTO = MusicCreateDTO(
            title = "Test Music",
            genres = setOf("POP", "ROCK"),
            musicFile = musicFile,
            imageFile = imageFile
        )
        val musicId = musicService.uploadMusic(userId, musicCreateDTO)

        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        val successResults = CopyOnWriteArrayList<Long>()
        val failureResults = CopyOnWriteArrayList<Throwable>()

        // when
        val startTime = System.currentTimeMillis()

        repeat(threads) { thread ->
            executorService.execute {
                try {
                    val randomUserId = ThreadLocalRandom.current().nextLong(1, threads.toLong())
                    val result = musicService.changeLikesFlag(musicId, thread.toLong())
                    result?.let { successResults.add(it) }

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
        failureResults.forEach { println(it) }
        println("테스트 실행 시간 : ${System.currentTimeMillis() - startTime} 밀리초")

        assertEquals(
            musicRepository.findById(musicId).get().likes,
            musicLikesRepository.findMusicLikesByFlag(true).size,
            "무작위 $threads 명의 음원 좋아요에 모두 성공해야 합니다."
        )
        assertEquals(0, failureResults.size, "음원 좋아요에 실패한 thread 는 없어야 합니다.")
    }
}