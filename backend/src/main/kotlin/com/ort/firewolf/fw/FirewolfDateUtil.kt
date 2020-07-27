package com.ort.firewolf.fw

import java.time.LocalDate
import java.time.LocalDateTime


class FirewolfDateUtil private constructor() {
    companion object {
        fun currentLocalDateTime(): LocalDateTime {
            return LocalDateTime.now()
        }

        fun currentLocalDate(): LocalDate {
            return LocalDate.now()
        }
    }
}