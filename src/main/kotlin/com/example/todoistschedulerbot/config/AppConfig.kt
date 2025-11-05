package com.example.todoistschedulerbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
data class AppConfig(
    var timezone: String = "Europe/Moscow",
    var defaultAppointment: DefaultAppointmentConfig = DefaultAppointmentConfig(),
    var users: UsersConfig = UsersConfig()
) {
    data class DefaultAppointmentConfig(
        var duration: DurationConfig = DurationConfig(),
        var time: TimeConfig = TimeConfig()
    ) {
        data class DurationConfig(
            var minutes: Int = 90
        )

        data class TimeConfig(
            var hour: Int = 14,
            var minute: Int = 0
        )
    }

    data class UsersConfig(
        var allowed: String = "",
        var config: String = "{}"
    )
}
