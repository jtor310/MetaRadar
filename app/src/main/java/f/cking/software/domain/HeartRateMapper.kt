package f.cking.software.domain

import f.cking.software.data.database.entity.HeartRateReadingEntity
import f.cking.software.domain.model.HeartRateReading
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun HeartRateReading.toData(): HeartRateReadingEntity {
    return HeartRateReadingEntity(
        id = id,
        deviceAddress = deviceAddress,
        heartRate = heartRate,
        timestamp = timestamp,
        contactDetected = contactDetected,
        energyExpended = energyExpended,
        rrIntervals = rrIntervals?.let { Json.encodeToString(it) }
    )
}

fun HeartRateReadingEntity.toDomain(): HeartRateReading {
    return HeartRateReading(
        id = id,
        deviceAddress = deviceAddress,
        heartRate = heartRate,
        timestamp = timestamp,
        contactDetected = contactDetected,
        energyExpended = energyExpended,
        rrIntervals = rrIntervals?.let {
            try {
                Json.decodeFromString<List<Int>>(it)
            } catch (e: Exception) {
                null
            }
        }
    )
}
