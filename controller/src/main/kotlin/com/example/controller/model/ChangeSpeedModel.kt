package com.example.controller.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ChangeSpeedModel(

    @field:NotBlank
    val topic: String,

    @field:Min(3_600)
    @field:Max(3_600_000_000)
    val speed: Long,
)
