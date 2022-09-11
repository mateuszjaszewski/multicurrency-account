package pl.mj.multicurrencyaccount.domain

import java.time.Clock
import java.time.LocalDate
import java.time.Period

data class Owner(val pesel: Pesel, val firstName: String, val lastName: String) {
    init {
        if (firstName.isBlank()) {
            throw InvalidOwnerDataException("Owner first name cannot be blank")
        }
        if (lastName.isBlank()) {
            throw InvalidOwnerDataException("Owner last name cannot be blank")
        }
    }

    fun age(clock: Clock): Int {
        return Period.between(pesel.birthDate(), LocalDate.now(clock)).years
    }

    class InvalidOwnerDataException(message: String) : DomainException(message)
}