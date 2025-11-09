package f.cking.software.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import f.cking.software.data.database.entity.HeartRateReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateReadingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReading(reading: HeartRateReadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadings(readings: List<HeartRateReadingEntity>)

    @Query("""
        SELECT * FROM heart_rate_reading
        WHERE device_address = :deviceAddress
        AND timestamp BETWEEN :fromTime AND :toTime
        ORDER BY timestamp DESC
    """)
    suspend fun getReadingsByDevice(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): List<HeartRateReadingEntity>

    @Query("""
        SELECT * FROM heart_rate_reading
        WHERE device_address = :deviceAddress
        AND timestamp BETWEEN :fromTime AND :toTime
        ORDER BY timestamp DESC
    """)
    fun observeReadingsByDevice(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Flow<List<HeartRateReadingEntity>>

    @Query("""
        SELECT * FROM heart_rate_reading
        WHERE device_address = :deviceAddress
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getLastNReadings(deviceAddress: String, limit: Int): List<HeartRateReadingEntity>

    @Query("""
        SELECT COUNT(*) FROM heart_rate_reading
        WHERE device_address = :deviceAddress
    """)
    suspend fun getReadingsCount(deviceAddress: String): Int

    @Query("""
        SELECT AVG(heart_rate) FROM heart_rate_reading
        WHERE device_address = :deviceAddress
        AND timestamp BETWEEN :fromTime AND :toTime
    """)
    suspend fun getAverageHeartRate(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Double?

    @Query("""
        SELECT MIN(heart_rate) FROM heart_rate_reading
        WHERE device_address = :deviceAddress
        AND timestamp BETWEEN :fromTime AND :toTime
    """)
    suspend fun getMinHeartRate(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Int?

    @Query("""
        SELECT MAX(heart_rate) FROM heart_rate_reading
        WHERE device_address = :deviceAddress
        AND timestamp BETWEEN :fromTime AND :toTime
    """)
    suspend fun getMaxHeartRate(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Int?

    @Query("DELETE FROM heart_rate_reading WHERE device_address = :deviceAddress")
    suspend fun deleteReadingsByDevice(deviceAddress: String)

    @Query("DELETE FROM heart_rate_reading WHERE timestamp < :beforeTime")
    suspend fun deleteReadingsOlderThan(beforeTime: Long)

    @Query("DELETE FROM heart_rate_reading")
    suspend fun deleteAllReadings()
}
