package com.ort.firewolf.application.service

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.service.message.MessageDomainService
import com.ort.firewolf.infrastructure.repository.DiscordRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

class NotificationServiceTest {
    private val discordRepository: DiscordRepository = mock(DiscordRepository::class.java)
    private val notificationService =
        NotificationService(discordRepository, mock(MessageDomainService::class.java))

    @AfterEach
    fun tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }

    @Test
    fun escapeDiscordMarkdown() {
        assertEquals("アリス", escapeDiscordMarkdown("アリス"))
        assertEquals("\\@everyone", escapeDiscordMarkdown("@everyone"))
        assertEquals("\\<\\@&123\\>", escapeDiscordMarkdown("<@&123>"))
        assertEquals("\\[click\\]\\(https\\://example.com\\)", escapeDiscordMarkdown("[click](https://example.com)"))
        assertEquals("\\*\\*bold\\*\\* \\_u\\_ \\~s\\~ \\|\\|x\\|\\| \\`c\\`", escapeDiscordMarkdown("**bold** _u_ ~s~ ||x|| `c`"))
        assertEquals("\\# 見出し", escapeDiscordMarkdown("# 見出し"))
        assertEquals("a\\\\\\*b", escapeDiscordMarkdown("a\\*b"))
    }

    @Test
    @DisplayName("国主宛ての発言はトランザクション中には送らず commit 後に送る")
    fun notifyToDeveloperPostsAfterCommit() {
        TransactionSynchronizationManager.initSynchronization()

        notificationService.notifyToDeveloperIfNeeded(1, message("@国主 よろしく"))

        verifyNoInteractions(discordRepository)
        afterCommit()
        verify(discordRepository).post(1, "\\@国主 よろしく")
    }

    @Test
    @DisplayName("国主宛ての発言はロールバックしたら送らない")
    fun notifyToDeveloperSkipsOnRollback() {
        TransactionSynchronizationManager.initSynchronization()

        notificationService.notifyToDeveloperIfNeeded(1, message("@国主 よろしく"))

        afterRollback()
        verifyNoInteractions(discordRepository)
    }

    @Test
    @DisplayName("国主宛ての発言はトランザクション外ならすぐに送る")
    fun notifyToDeveloperPostsImmediatelyWithoutTransaction() {
        notificationService.notifyToDeveloperIfNeeded(1, message("＠国主 よろしく"))

        verify(discordRepository).post(1, "＠国主 よろしく")
    }

    @Test
    @DisplayName("国主宛てでない発言は送らない")
    fun notifyToDeveloperSkipsMessageWithoutMention() {
        TransactionSynchronizationManager.initSynchronization()

        notificationService.notifyToDeveloperIfNeeded(1, message("よろしく"))

        assertEquals(0, TransactionSynchronizationManager.getSynchronizations().size)
        afterCommit()
        verifyNoInteractions(discordRepository)
    }

    @Test
    @DisplayName("開発者へのテキスト通知はトランザクション中には送らず commit 後に送る")
    fun notifyToDeveloperTextPostsAfterCommit() {
        val village = mock(Village::class.java)
        `when`(village.id).thenReturn(2)
        TransactionSynchronizationManager.initSynchronization()

        notificationService.notifyToDeveloperTextIfNeeded(village, "IPアドレス重複検出: 192.0.2.1")

        verify(discordRepository, never()).post(2, "IPアドレス重複検出\\: 192.0.2.1")
        afterCommit()
        verify(discordRepository).post(2, "IPアドレス重複検出\\: 192.0.2.1")
    }

    @Test
    @DisplayName("開発者へのテキスト通知はロールバックしたら送らない")
    fun notifyToDeveloperTextSkipsOnRollback() {
        val village = mock(Village::class.java)
        `when`(village.id).thenReturn(2)
        TransactionSynchronizationManager.initSynchronization()

        notificationService.notifyToDeveloperTextIfNeeded(village, "IPアドレス重複検出: 192.0.2.1")

        afterRollback()
        verifyNoInteractions(discordRepository)
    }

    @Test
    @DisplayName("開発者へのテキスト通知はトランザクション外ならすぐに送る")
    fun notifyToDeveloperTextPostsImmediatelyWithoutTransaction() {
        val village = mock(Village::class.java)
        `when`(village.id).thenReturn(2)

        notificationService.notifyToDeveloperTextIfNeeded(village, "IPアドレス重複検出: 192.0.2.1")

        verify(discordRepository).post(2, "IPアドレス重複検出\\: 192.0.2.1")
    }

    private fun message(text: String): Message {
        val content = MessageContent.invoke(CDef.MessageType.通常発言.code(), text, CDef.FaceType.通常.code())
        val message = mock(Message::class.java)
        `when`(message.content).thenReturn(content)
        `when`(message.shouldNotify()).thenReturn(content.shouldNotify())
        return message
    }

    // commit 時に Spring が呼ぶ順序 (afterCommit → afterCompletion) を再現する
    private fun afterCommit() {
        val synchronizations = TransactionSynchronizationManager.getSynchronizations()
        synchronizations.forEach { it.afterCommit() }
        synchronizations.forEach { it.afterCompletion(TransactionSynchronization.STATUS_COMMITTED) }
    }

    // ロールバック時は afterCommit を呼ばず afterCompletion だけが呼ばれる
    private fun afterRollback() {
        TransactionSynchronizationManager.getSynchronizations()
            .forEach { it.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK) }
    }
}
