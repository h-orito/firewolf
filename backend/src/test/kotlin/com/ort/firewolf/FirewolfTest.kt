package com.ort.firewolf

import com.nhaarman.mockitokotlin2.whenever
import com.ort.firewolf.fw.FirewolfDateUtil
import com.ort.firewolf.fw.FirewolfUserInfoUtil
import com.ort.firewolf.fw.config.FirebaseConfig
import com.ort.firewolf.fw.security.FirewolfUser
import org.dbflute.hook.AccessContext
import org.junit.Before
import org.springframework.boot.test.mock.mockito.MockBean


open class FirewolfTest {

    @MockBean
    lateinit var firebaseConfig: FirebaseConfig

    @Before
    fun setUp() {
        // firebase関連はmockにする
        whenever(firebaseConfig.init()).then { }
        whenever(firebaseConfig.firebaseDatabase()).thenReturn(null)
        // set access context
        setAccessContext()
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun setAccessContext() {
        if (AccessContext.isExistAccessContextOnThread()) {
            // 既に設定されていたら何もしないで次へ
            // (二度呼び出しされたときのために念のため)
            return
        }
        // [アクセス日時]
        val accessLocalDateTime = FirewolfDateUtil.currentLocalDateTime()

        // [アクセスユーザ]
        val userInfo: FirewolfUser? = FirewolfUserInfoUtil.getUserInfo()
        val accessUser = userInfo?.username ?: "not login user"

        val context = AccessContext()
        context.accessLocalDateTime = accessLocalDateTime
        context.accessUser = accessUser
        AccessContext.setAccessContextOnThread(context)
    }
}