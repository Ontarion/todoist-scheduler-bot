package com.example.todoistschedulerbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "todoist.api")
data class TodoistConfig(
    var baseUrl: String = "https://api.todoist.com/rest/v2"
)
