package f.cking.software.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "heart_rate_reading",
    indices = [
        Index(value = ["device_address"]),
        Index(value = ["timestamp"]),
        Index(value = ["device_address", "timestamp"])
    ]
)
data class HeartRateReadingEntity(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "device_address") val deviceAddress: String,
    @ColumnInfo(name = "heart_rate") val heartRate: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "contact_detected") val contactDetected: Boolean?,
    @ColumnInfo(name = "energy_expended") val energyExpended: Int?,
    @ColumnInfo(name = "rr_intervals") val rrIntervals: String?, // JSON array stored as string
)
