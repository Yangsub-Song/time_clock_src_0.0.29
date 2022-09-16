package com.techrove.timeclock.server.cwma

import com.techrove.timeclock.server.cwma.converter.Encrypt
import com.techrove.timeclock.server.cwma.model.req.*
import com.techrove.timeclock.server.cwma.model.res.Response
import com.techrove.timeclock.server.cwma.model.res.SecureKeyRegisterConfirmResponse
import com.techrove.timeclock.server.cwma.model.res.ServerTimeResponse
import com.techrove.timeclock.server.cwma.model.res.WorkerInfoResponse
import retrofit2.http.*

@Suppress("FunctionName", "NonAsciiCharacters")
interface CwmaService {

    /**
     * [보안키등록확인](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=1383457993)
     */
    @POST("auth/v2/authKey.do")
    suspend fun 보안키등록확인(
        @Encrypt @Query("data") kmsId: KmsId = KmsId()
    ): SecureKeyRegisterConfirmResponse

    /**
     * [단말기상태](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=707021448)
     *
     * 인증 단말기의 상태를 서버에 전송하는 전문. (상태 전송 주기는 60분)
     * - 서버에서는 20분내에 단말기 상태를 수신한 경우 해당 단말기는 온라인으로 간주함
     */
    @POST("auth/v2/status.do")
    suspend fun 단말기상태(
        @Encrypt @Query("data") terminalStatus: TerminalStatus = TerminalStatus()
    ): Response

    /**
     * [시간조회](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=2136672566)
     *
     * 서버 시간 조회
     * 주의 : 시간 조회는 필요시(예: 단말기 부팅시, 오프라인에서 온라인 전환시 등)
     * 시간 동기화를 위해서 사용하는것을 원칙으로 하고, 주기적으로 연동하지 않도록 주의 바람
     */
    @POST("auth/v2/syncDatetime.do")
    suspend fun 시간조회(
        @Encrypt @Query("data") kmsId: KmsId = KmsId()
    ): ServerTimeResponse

    /**
     * [서버응답체크](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=1026865701)
     *
     * 서버 응답 여부 체크 (온라인 상태)
     * 주의 : 서버 응답 여부는 주기적으로 체크하는것이 아니라, 단말기가 어떠한 이유로 인해
     * 오프라인 상태로 진입하고, 오프라인 상태에서 수집된 인증 정보를 송신할 시기를 파악하기 위해서 사용
     * 단말기와 서버의 연결이 온라인 상태일때는 이용 불가,
     * 오프라인 상태일때만 온라인으로 돌아왔는지 확인을 위해서 10~30분주기로 사용
     */
    @POST("auth/v2/checkServer.do")
    suspend fun 서버응답체크(
        @Encrypt @Query("data") kmsId: KmsId = KmsId()
    ): Response

    /**
     * [근로자정보 조회등록](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=784994587)
     *
     * 근로자의 정보 요청 및 정보가 없을시 간단 정보(주민등록번호)를 등록한다.
     */
    @POST("auth/v2/workerInfo.do")
    suspend fun 근로자정보조회등록(
        @Encrypt @Query("data") workerInfo: WorkerInfoRequest
    ): WorkerInfoResponse


    /**
     * [지문 등록](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=1514244977)
     *
     * 근로자의 지문 정보를 등록한다.
     */
    @POST("auth/v2/registFinger.do")
    suspend fun 지문등록(
        @Encrypt @Query("data") fingerInfo: RegisterFingerRequest
    ): Response


    ///////////////////////////////////////////////////////////////////////////
    // 지문 인증 출퇴근
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [온라인 지문 인증_출근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=918400498)
     *
     * 근로자의 지문 정보를 서버에 전송하여 출근 처리한다.
     */
    @POST("auth/v2/onlineAuthFingerStart.do")
    suspend fun 온라인지문인증출근(
        @Encrypt @Query("data") fingerAuth: FingerAuthRequest
    ): Response

    /**
     * [온라인 지문 인증_퇴근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=2104286180)
     *
     * 근로자의 지문 정보를 서버에 전송하여 출근 처리한다.
     */
    @POST("auth/v2/onlineAuthFingerEnd.do")
    suspend fun 온라인지문인증퇴근(
        @Encrypt @Query("data") fingerAuth: FingerAuthRequest
    ): Response

    /**
     * [오프라인 지문 인증_출근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=1257638477)
     *
     * 단말에 저장된 근로자의 지문 인증 결과를 서버에 전송하여 출근 처리한다.
     * 주의 : 서버응답체크를 통해서 온라인 상태로 돌아왔을때 전송을 시작하며, 5최 간격으로 전송 필요
     */
    @POST("auth/v2/offlineAuthFingerStart.do")
    suspend fun 오프라인지문인증출근(
        @Encrypt @Query("data") fingerAuth: FingerAuthRequest
    ): Response

    /**
     * [오프라인 지문 인증_퇴근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=1190395646)
     *
     * 단말에 저장된 근로자의 지문 인증 결과를 서버에 전송하여 퇴근 처리한다.
     * 주의 : 서버응답체크를 통해서 온라인 상태로 돌아왔을때 전송을 시작하며, 5최 간격으로 전송 필요
     */
    @POST("auth/v2/offlineAuthFingerEnd.do")
    suspend fun 오프라인지문인증퇴근(
        @Encrypt @Query("data") fingerAuth: FingerAuthRequest
    ): Response

    ///////////////////////////////////////////////////////////////////////////
    // 카드 인증 출퇴근
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [온라인 카드 인증_출근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=355938693)
     *
     * 근로자의 카드 정보를 서버에 전송하여 출근 처리한다.
     * - 근로자정보 등록, 출결 정보를 동시에 처리한다.
     */
    @POST("auth/v2/onlineAuthCardStart.do")
    suspend fun 온라인카드인증출근(
        @Encrypt @Query("data") cardAuth: CardAuthRequest
    ): Response

    /**
     * [온라인 카드 인증_퇴근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=608948881)
     *
     * 근로자의 카드 정보를 서버에 전송하여 퇴근 처리한다.
     * - 근로자정보 등록, 출결 정보를 동시에 처리한다.
     */
    @POST("auth/v2/onlineAuthCardEnd.do")
    suspend fun 온라인카드인증퇴근(
        @Encrypt @Query("data") cardAuth: CardAuthRequest
    ): Response

    /**
     * [오프라인 카드 인증_출근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=1382746047)
     *
     * 단말에 저장된 근로자의 카드 인증 결과를 서버에 전송하여 출근 처리한다.
     * 주의 : 서버응답체크를 통해서 온라인 상태로 돌아왔을때 전송을 시작하며, 5최 간격으로 전송 필요
     */
    @POST("auth/v2/offlineAuthCardStart.do")
    suspend fun 오프라인카드인증출근(
        @Encrypt @Query("data") cardAuth: CardAuthRequest
    ): Response

    /**
     * [오프라인 카드 인증_퇴근](https://docs.google.com/spreadsheets/d/1f_o-zznTVGuJyJtvq4FKmS0ebsGngeaWG-kHj5LJe2s/edit#gid=437903813)
     *
     * 단말에 저장된 근로자의 카드 인증 결과를 서버에 전송하여 퇴근 처리한다.
     * 주의 : 서버응답체크를 통해서 온라인 상태로 돌아왔을때 전송을 시작하며, 5최 간격으로 전송 필요
     */
    @POST("auth/v2/offlineAuthCardEnd.do")
    suspend fun 오프라인카드인증퇴근(
        @Encrypt @Query("data") cardAuth: CardAuthRequest
    ): Response


}
