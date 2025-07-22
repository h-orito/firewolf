package com.ort.firewolf.fw.interceptor

import com.ort.firewolf.fw.FirewolfDateUtil
import com.ort.firewolf.fw.FirewolfUserInfoUtil
import com.ort.firewolf.fw.security.FirewolfUser
import com.ort.firewolf.fw.security.getIpAddress
import org.dbflute.hook.AccessContext
import org.springframework.web.servlet.HandlerInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse


class FirewolfAccessContextInterceptor : HandlerInterceptor {

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // [アクセス日時]
        val accessLocalDateTime = FirewolfDateUtil.currentLocalDateTime()

        // [アクセスユーザ]
        val userInfo: FirewolfUser? = FirewolfUserInfoUtil.getUserInfo()
        val accessUser = userInfo?.username ?: "not_login_user"

        val context = AccessContext()
        context.accessLocalDateTime = accessLocalDateTime
        context.accessUser = "$accessUser: ${request.getIpAddress()}"
        AccessContext.setAccessContextOnThread(context)

        // Handlerメソッドを呼び出す場合はtrueを返却する
        return true
    }
}