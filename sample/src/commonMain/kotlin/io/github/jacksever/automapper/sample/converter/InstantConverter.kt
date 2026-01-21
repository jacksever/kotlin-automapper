package io.github.jacksever.automapper.sample.converter

import io.github.jacksever.automapper.annotation.AutoConverter
import kotlin.time.Instant

/**
 * Provides custom type converters for handling [Instant] objects
 */
internal object InstantConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = value)
}
