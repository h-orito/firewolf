package com.ort.firewolf.fw.interceptor

import com.ort.firewolf.fw.FirewolfDateUtil
import com.ort.firewolf.fw.FirewolfUserInfoUtil
import com.ort.firewolf.fw.security.FirewolfUser
import org.dbflute.hook.AccessContext
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class FirewolfAccessContextInterceptor : HandlerInterceptorAdapter() {

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // [アクセス日時]
        val accessLocalDateTime = FirewolfDateUtil.currentLocalDateTime()

        // [アクセスユーザ]
        val userInfo: FirewolfUser? = FirewolfUserInfoUtil.getUserInfo()
        val accessUser = userInfo?.username ?: "not_login_user"
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val ipAddress =
            if (xForwardedFor.isNullOrEmpty()) request.remoteAddr
            else xForwardedFor

        val context = AccessContext()
        context.accessLocalDateTime = accessLocalDateTime
        context.accessUser = "$accessUser: $ipAddress"
        AccessContext.setAccessContextOnThread(context)

        // Handlerメソッドを呼び出す場合はtrueを返却する
        return true
    }
}