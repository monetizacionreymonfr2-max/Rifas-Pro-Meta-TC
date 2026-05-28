package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RaffleDao {
    @Query("SELECT * FROM raffles ORDER BY createdAt DESC")
    fun getAllRaffles(): Flow<List<Raffle>>

    @Query("SELECT * FROM raffles WHERE id = :id")
    fun getRaffleByIdFlow(id: Int): Flow<Raffle?>

    @Query("SELECT * FROM raffles WHERE id = :id")
    suspend fun getRaffleById(id: Int): Raffle?

    @Query("SELECT * FROM tickets_sold WHERE raffleId = :raffleId ORDER BY number ASC")
    fun getTicketsSoldForRaffle(raffleId: Int): Flow<List<TicketSold>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaffle(raffle: Raffle): Long

    @Delete
    suspend fun deleteRaffle(raffle: Raffle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicketSold(ticketSold: TicketSold): Long

    @Query("DELETE FROM tickets_sold WHERE id = :ticketId")
    suspend fun deleteTicketSold(ticketId: Int)

    @Query("DELETE FROM tickets_sold WHERE raffleId = :raffleId AND number = :number")
    suspend fun deleteTicketByNumber(raffleId: Int, number: Int)

    @Update
    suspend fun updateTicketSold(ticketSold: TicketSold)
}
