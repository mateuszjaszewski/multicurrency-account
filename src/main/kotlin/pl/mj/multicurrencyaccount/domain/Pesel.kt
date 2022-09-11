package pl.mj.multicurrencyaccount.domain

import java.time.LocalDate

data class Pesel(val value: String) {

    init {
        if (!isValid()) {
            throw InvalidPeselException(value)
        }
    }

    fun birthDate() : LocalDate {
        val yearValue = value.substring(0, 2).toInt()
        val monthValue = value.substring(2, 4).toInt()
        val day = value.substring(4, 6).toInt()
        val (baseYear, monthDiff) = when (monthValue) {
            in 1..12 -> Pair(1900, 0)
            in 21..32 -> Pair(2000, 20)
            in 41..52 -> Pair(2100, 40)
            in 61..72 -> Pair(2200, 60)
            in 81..92 -> Pair(1800, 80)
            else -> throw RuntimeException("Cannot calculate birth date")
        }
        return LocalDate.of(baseYear + yearValue, monthValue - monthDiff, day)
    }

    override fun toString() = value

    private fun isValid() : Boolean {
        if (!value.matches(Regex("^[0-9]{11}$"))) {
            return false
        }
        val weights = listOf(1, 3, 7, 9, 1, 3, 7, 9, 1, 3, 1)
        val digits = value.map { char -> char.minus('0') }
        val checksum = (0..10).sumOf { weights[it] * digits[it] } % 10
        return checksum == 0
    }

    class InvalidPeselException(pesel: String) : DomainException("Pesel $pesel is invalid")
}
