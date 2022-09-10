package pl.mj.multicurrencyaccount.domain

import java.time.Clock
import java.time.LocalDate
import java.time.Period

data class Owner(val pesel: Pesel, val firstName: String, val lastName: String) {
    fun age(clock: Clock) = Period.between(pesel.birthDate(), LocalDate.now(clock)).years
}