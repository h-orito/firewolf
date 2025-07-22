package com.ort.firewolf.fw.config

import com.ort.firewolf.fw.interceptor.FirewolfAccessContextInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

class FirewolfWebMvcConfigurer : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(FirewolfAccessContextInterceptor()).addPathPatterns("/**")
    }
}