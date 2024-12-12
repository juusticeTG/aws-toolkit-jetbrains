// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.core.notifications

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import software.aws.toolkits.core.utils.ETagProvider
import java.time.Duration
import java.time.Instant

class InstantConverter : Converter<Instant>() {
    override fun toString(value: Instant): String = value.toEpochMilli().toString()

    override fun fromString(value: String): Instant = Instant.ofEpochMilli(value.toLong())
}

data class DismissedNotification(
    @Attribute
    val id: String = "",
    @Attribute(converter = InstantConverter::class)
    val dismissedAt: Instant = Instant.now(),
)

data class NotificationDismissalConfiguration(
    @Property
    var dismissedNotifications: MutableSet<DismissedNotification> = mutableSetOf(),
)

@Service
@State(name = "notificationDismissals", storages = [Storage("aws.xml", roamingType = RoamingType.DISABLED)])
class NotificationDismissalState : PersistentStateComponent<NotificationDismissalConfiguration> {
    private val state = NotificationDismissalConfiguration()
    private val retentionPeriod = Duration.ofDays(60) // 2 months

    override fun getState(): NotificationDismissalConfiguration = state

    override fun loadState(state: NotificationDismissalConfiguration) {
        this.state.dismissedNotifications.clear()
        this.state.dismissedNotifications.addAll(state.dismissedNotifications)
        cleanExpiredNotifications()
    }

    fun isDismissed(notificationId: String): Boolean =
        state.dismissedNotifications.any { it.id == notificationId }

    fun dismissNotification(notificationId: String) {
        state.dismissedNotifications.add(
            DismissedNotification(
                id = notificationId
            )
        )
    }

    private fun cleanExpiredNotifications() {
        val now = Instant.now()
        state.dismissedNotifications.removeAll { notification ->
            Duration.between(notification.dismissedAt, now) > retentionPeriod
        }
    }

    companion object {
        fun getInstance(): NotificationDismissalState = service()
    }
}

@Service
@State(name = "notificationEtag", storages = [Storage("aws.xml", roamingType = RoamingType.DISABLED)])
class NotificationEtagState : PersistentStateComponent<NotificationEtagConfiguration>, ETagProvider {
    private val state = NotificationEtagConfiguration()

    override fun updateETag(newETag: String?) {
        etag = newETag
    }

    override fun getState(): NotificationEtagConfiguration = state

    override fun loadState(state: NotificationEtagConfiguration) {
        this.state.etag = state.etag
    }

    override var etag: String?
        get() = state.etag
        set(value) {
            state.etag = value
        }

    companion object {
        fun getInstance(): NotificationEtagState =
            service()
    }
}

data class NotificationEtagConfiguration(
    var etag: String? = null,
)

@Service
class BannerNotificationService {
    private val notifications = mutableMapOf<String, BannerContent>()

    fun addNotification(id: String, content: BannerContent) {
        notifications[id] = content
    }

    fun getNotifications(): Map<String, BannerContent> = notifications

    fun removeNotification(id: String) {
        notifications.remove(id)
    }

    companion object {
        fun getInstance(): BannerNotificationService =
            service()
    }
}
