package f.cking.software.domain.interactor

import timber.log.Timber

/**
 * Parses BLE Heart Rate Measurement characteristic (0x2A37) according to the
 * Bluetooth SIG Heart Rate Service specification.
 *
 * Data format:
 * - Byte 0: Flags
 *   - Bit 0: Heart Rate Value Format (0=UINT8, 1=UINT16)
 *   - Bit 1-2: Sensor Contact Status (00/01=not supported, 10=not detected, 11=detected)
 *   - Bit 3: Energy Expended Status (0=not present, 1=present)
 *   - Bit 4: RR-Interval Status (0=not present, 1=present)
 * - Byte 1-2: Heart Rate Value (8 or 16 bit based on flag)
 * - Byte 3-4: Energy Expended (if flag bit 3 is set)
 * - Byte 5+: RR-Intervals (if flag bit 4 is set, multiple 16-bit values in 1/1024 second resolution)
 */
class ParseHeartRateMeasurement {

    fun execute(data: ByteArray): HeartRateMeasurement? {
        if (data.isEmpty()) {
            Timber.tag(TAG).w("Heart rate measurement data is empty")
            return null
        }

        try {
            val flags = data[0].toInt() and 0xFF

            // Parse heart rate value
            val isHeartRateUint16 = (flags and FLAG_HEART_RATE_VALUE_FORMAT) != 0
            val heartRateOffset = 1

            val heartRate = if (isHeartRateUint16) {
                if (data.size < 3) {
                    Timber.tag(TAG).w("Insufficient data for UINT16 heart rate")
                    return null
                }
                // Little-endian UINT16
                (data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)
            } else {
                if (data.size < 2) {
                    Timber.tag(TAG).w("Insufficient data for UINT8 heart rate")
                    return null
                }
                data[1].toInt() and 0xFF
            }

            var currentOffset = if (isHeartRateUint16) 3 else 2

            // Parse sensor contact status
            val sensorContactSupported = (flags and FLAG_SENSOR_CONTACT_SUPPORTED) != 0
            val sensorContactDetected = if (sensorContactSupported) {
                (flags and FLAG_SENSOR_CONTACT_DETECTED) != 0
            } else {
                null
            }

            // Parse energy expended
            val energyExpendedPresent = (flags and FLAG_ENERGY_EXPENDED_PRESENT) != 0
            val energyExpended = if (energyExpendedPresent) {
                if (data.size < currentOffset + 2) {
                    Timber.tag(TAG).w("Insufficient data for energy expended")
                    null
                } else {
                    val value = (data[currentOffset].toInt() and 0xFF) or
                                ((data[currentOffset + 1].toInt() and 0xFF) shl 8)
                    currentOffset += 2
                    value
                }
            } else {
                null
            }

            // Parse RR-Intervals
            val rrIntervalsPresent = (flags and FLAG_RR_INTERVAL_PRESENT) != 0
            val rrIntervals = if (rrIntervalsPresent) {
                val intervals = mutableListOf<Int>()
                while (currentOffset + 1 < data.size) {
                    // RR-Intervals are in 1/1024 second resolution
                    val interval = (data[currentOffset].toInt() and 0xFF) or
                                   ((data[currentOffset + 1].toInt() and 0xFF) shl 8)
                    intervals.add(interval)
                    currentOffset += 2
                }
                intervals.takeIf { it.isNotEmpty() }
            } else {
                null
            }

            Timber.tag(TAG).d("Parsed heart rate: $heartRate BPM, contact: $sensorContactDetected, energy: $energyExpended, RR intervals: ${rrIntervals?.size ?: 0}")

            return HeartRateMeasurement(
                heartRate = heartRate,
                sensorContactDetected = sensorContactDetected,
                energyExpended = energyExpended,
                rrIntervals = rrIntervals
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error parsing heart rate measurement")
            return null
        }
    }

    data class HeartRateMeasurement(
        val heartRate: Int,
        val sensorContactDetected: Boolean?,
        val energyExpended: Int?,
        val rrIntervals: List<Int>?
    )

    companion object {
        private const val TAG = "ParseHeartRateMeasurement"

        // Flag bit masks
        private const val FLAG_HEART_RATE_VALUE_FORMAT = 0x01  // 0 = UINT8, 1 = UINT16
        private const val FLAG_SENSOR_CONTACT_SUPPORTED = 0x04  // Bit 2
        private const val FLAG_SENSOR_CONTACT_DETECTED = 0x02   // Bit 1
        private const val FLAG_ENERGY_EXPENDED_PRESENT = 0x08   // Bit 3
        private const val FLAG_RR_INTERVAL_PRESENT = 0x10       // Bit 4
    }
}
