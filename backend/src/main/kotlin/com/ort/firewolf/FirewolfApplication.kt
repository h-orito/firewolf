package com.ort.firewolf

import com.ort.dbflute.allcommon.DBFluteBeansJavaConfig
import com.ort.firewolf.fw.config.FirebaseConfig
import com.ort.firewolf.fw.config.FirewolfAppConfig
import com.ort.firewolf.fw.config.FirewolfWebMvcConfigurer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(
    DBFluteBeansJavaConfig::class,
    FirewolfAppConfig::class,
    FirewolfWebMvcConfigurer::class,
    FirebaseConfig::class
)
@EnableCaching
class FirewolfApplication

fun main(args: Array<String>) {
    runApplication<FirewolfApplication>(*args)
}