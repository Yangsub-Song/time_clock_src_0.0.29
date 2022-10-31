package com.techrove.timeclock.server.admin

import com.techrove.timeclock.server.admin.model.req.AdminTerminalStatus
import com.techrove.timeclock.server.admin.model.res.AdminTerminalStatusResponse
import com.techrove.timeclock.server.cwma.converter.Encrypt
import retrofit2.http.POST
import retrofit2.http.Query

@Suppress("FunctionName", "NonAsciiCharacters")
interface AdminService {

    @POST("status")
    suspend fun 단말기상태(
        @Encrypt @Query("data") status: AdminTerminalStatus
    ): AdminTerminalStatusResponse
}
