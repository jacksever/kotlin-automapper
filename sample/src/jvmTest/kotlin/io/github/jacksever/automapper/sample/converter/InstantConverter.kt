package io.github.jacksever.automapper.sample.converter

import io.github.jacksever.automapper.annotation.AutoConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal object InstantConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = value)
}

@OptIn(ExperimentalTime::class)
internal object GlobalPriorityConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = 111L

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = 111L)
}

@OptIn(ExperimentalTime::class)
internal object LocalPriorityConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = 999L

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = 999L)
}
