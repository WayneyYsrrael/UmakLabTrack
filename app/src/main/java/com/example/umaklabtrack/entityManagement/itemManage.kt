package com.example.umaklabtrack.entityManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import com.example.umaklabtrack.supabaseHandler.SupabaseConnection
import com.example.umaklabtrack.dataClasses.Items
import com.example.umaklabtrack.dataClasses.UserSession
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.coroutines.*
class ItemManage(private val spb: SupabaseConnection = SupabaseConnection()) {

    private val supabaseClient = spb.supabase
    var reservationVar: Int = 0

    fun fetchAllItems(onResult: (List<Items>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val itemsFromDb = supabaseClient.postgrest
                    .from("item")
                    .select()
                    .decodeList<Items>() ?: emptyList()

                CoroutineScope(Dispatchers.Main).launch {
                    onResult(itemsFromDb)

                }

            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(emptyList())
                }
            }
        }
    }


    @Serializable
    data class BorrowedItem(
        val reservation_id: Int,
        val item_id: Int,
        val item_name: String,
        val quantity: Int
    )

    @Serializable
    data class Item(
        val item_id: Int,
        val name: String,
    )

    suspend fun getItemIdByName(itemName: String): Int? {
        return try {
            val result: List<Item> = supabaseClient.postgrest
                .from("item")
                .select(columns = Columns.list("item_id", "name")) {
                    filter { eq("name", itemName) }
                }
                .decodeList()
            result.firstOrNull()?.item_id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    suspend fun insertItemsToSupabase(
        reservationId: Int,
        selected: Map<String, Int>
    ) = coroutineScope {
        // Fetch all item IDs in parallel
        val itemsToInsert = selected.map { (name, quantity) ->
            async(Dispatchers.IO) {
                val itemId = getItemIdByName(name) ?: 1
                BorrowedItem(
                    reservation_id = reservationId,
                    item_id = itemId,
                    item_name = name,
                    quantity = quantity
                )
            }
        }.awaitAll()

        // Insert items sequentially (or can also be async if Supabase allows)
        itemsToInsert.forEach { item ->
            supabaseClient.postgrest["transacted_items"].insert(item)
        }

        println("Inserted items: $itemsToInsert")
    }


    @Serializable
    data class BorrowerInfo(
        val reservation_id: Int? = null,
        val user_id: String = "",
        val type: String = "",
        val return_by: String = "",
        val subject: String = "",
        val college: String = "",
        val yr_section: String = "",
        val borrowing_date: String = "",
        val created_at: String = "",
        val room: String = "",
        val student_representative_names: List<String> = emptyList()
    )

    suspend fun insertBorrowerInfo(
        subject: String,
        college: String,
        yearSection: String,
        selected: Map<String, Int>,
        types: String
    ): Int? {
        return try {
            val now = java.time.LocalDateTime.now()
            val returnBy = now.plusHours(3)

            val borrowerInfo = BorrowerInfo(
                user_id = UserSession.USER_ID!!,
                type = types,
                return_by = returnBy.toString(),
                subject = subject,
                college = college,
                yr_section = yearSection,
                borrowing_date = UserSession.pickup ?: now.toString(),
                created_at = now.toString(),
                room = UserSession.room!!,
                student_representative_names = UserSession.listStud!!
            )

            // Insert into reservation and get the inserted reservation_id
            val inserted: BorrowerInfo = supabaseClient.postgrest
                .from("reservation")
                .insert(borrowerInfo) {
                    select(columns = Columns.list("reservation_id"))
                }
                .decodeSingle()

            // Use the actual reservation_id returned
            val reservationId = inserted.reservation_id

            insertItemsToSupabase(
                reservationId = reservationId!!,
                selected = selected
            )

            if (reservationId != null) {
                UserSession.college = null
                UserSession.subject = null
                UserSession.yearSection = null
                UserSession.room = null
                UserSession.listStud = null
            }

            return reservationId

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Serializable
    data class TransactedItem(
        val item_id: Int,
        val transaction_id: Int,
        val reservation_id: Int,
        val item_name: String,
        val quantity: Int

    )

    suspend fun getLatestReservation(): BorrowInfo? {
        val response = supabaseClient.postgrest
            .from("reservation")
            .select()
            {
                filter { eq("user_id", UserSession.USER_ID!!) }
                order(column = "reservation_id", order = Order.DESCENDING)
                limit(1)
            }.decodeSingle<BorrowInfo>()
        return response
    }

    @Serializable
    data class BorrowInfo(
        val user_id: String,
        val type: String,
        val created_at: String,
        val status: String
        // reservation_id is intentionally omitted
    )

    @Serializable
    data class BorrowInfoWithId(
        val user_id: String,
        val type: String,
        val created_at: String,
        val status: String,
        val reservation_id: Int // use Int to match the DB and items
    )

    suspend fun getReservationsForUser(): List<BorrowInfoWithId> {
        return supabaseClient.postgrest
            .from("reservation")
            .select(
                columns = Columns.list(
                    "user_id",
                    "type",
                    "created_at",
                    "status",
                    "reservation_id"
                )
            ) {
                filter { eq("user_id", UserSession.USER_ID!!) }
                order(column = "reservation_id", order = Order.DESCENDING)
            }
            .decodeList() ?: emptyList()
    }

    // Optional: Map to BorrowInfo + reservation_id pair
    fun mapToBorrowInfoPair(list: List<BorrowInfoWithId>): List<Pair<BorrowInfo, Int>> {
        return list.map { borrow ->
            BorrowInfo(
                user_id = borrow.user_id,
                type = borrow.type,
                created_at = borrow.created_at,
                status = borrow.status
            ) to borrow.reservation_id
        }
    }

    // Function to get items for a reservation
    suspend fun getItemsForReservation(reservationId: Int): List<TransactedItem> {
        return supabaseClient.postgrest
            .from("transacted_items")
            .select(columns = Columns.list("transaction_id", "reservation_id", "item_name", "quantity","item_id")) {
                filter { eq("reservation_id", reservationId) }
            }
            .decodeList<TransactedItem>()
    }


    // Function to get reservations with their items
    suspend fun getReservationsWithItems(): Map<BorrowInfoWithId, List<TransactedItem>> {
        val reservations = getReservationsForUser()
        val map = mutableMapOf<BorrowInfoWithId, List<TransactedItem>>()

        for (reservation in reservations) {
            val items = getItemsForReservation(reservation.reservation_id)
            map[reservation] = items
        }

        return map
    }
    suspend fun getCategory(itemId: Int): String? {
        return try {
            val result: List<ItemL> = supabaseClient.postgrest
                .from("item")
                .select(columns = Columns.list("item_id", "category")) {
                    filter { eq("item_id", itemId) }
                }
                .decodeList()

            result.firstOrNull()?.category
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class ItemL(
        val item_id: Int,
        val category: String
    )


}