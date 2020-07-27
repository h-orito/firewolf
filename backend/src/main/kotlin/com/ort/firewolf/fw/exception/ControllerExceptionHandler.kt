package com.ort.firewolf.fw.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@RestControllerAdvice
class ControllerExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        var body = body
        if (body !is FirewolfErrorResponse) {
            body = FirewolfErrorResponse(status.value(), status.reasonPhrase)
        }
        return ResponseEntity(body, headers, status)
    }

    @ExceptionHandler(FirewolfBusinessException::class)
    fun handleBusinessException(ex: FirewolfBusinessException, request: WebRequest?): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = FirewolfErrorResponse(499, ex.message)
        val status = HttpStatus.NOT_FOUND // dummy
        return handleExceptionInternal(ex, body, headers, status, request!!)
    }

    @ExceptionHandler(FirewolfBadRequestException::class)
    fun handle400(ex: FirewolfBadRequestException, request: WebRequest?): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = FirewolfErrorResponse(400, ex.message)
        val status = HttpStatus.BAD_REQUEST
        return handleExceptionInternal(ex, body, headers, status, request!!)
    }

    @ExceptionHandler(Exception::class)
    fun handle500(ex: Exception, request: WebRequest?): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = null
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        return handleExceptionInternal(ex, body, headers, status, request!!)
    }
}