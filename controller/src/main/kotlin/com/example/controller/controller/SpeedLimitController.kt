package com.example.controller.controller

import com.example.controller.model.ChangeSpeedModel
import com.example.controller.service.SpeedSettings
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
class SpeedLimitController(
    val service: SpeedSettings,
) {
    @PostMapping("/change-speed")
    fun limit(@Valid @RequestBody data: ChangeSpeedModel) {
        service.modify(data.topic, data.speed)
    }
}
