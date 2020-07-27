package com.ort.firewolf.fw.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class FirewolfBadRequestException(override val message: String) : RuntimeException() {
}