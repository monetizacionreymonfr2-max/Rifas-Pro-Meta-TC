package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "raffles")
data class Raffle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val totalNumbers: Int,
    val description: String,
    val ticketPrice: Double = 0.0,
    val prize: String = "",
    val startFromZero: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tickets_sold",
    foreignKeys = [
        ForeignKey(
            entity = Raffle::class,
            parentColumns = ["id"],
            childColumns = ["raffleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["raffleId", "number"], unique = true)]
)
data class TicketSold(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val raffleId: Int,
    val number: Int,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String = "",
    val status: String = "PENDING", // PENDING, PAID
    val notes: String = "",
    val soldAt: Long = System.currentTimeMillis()
)
