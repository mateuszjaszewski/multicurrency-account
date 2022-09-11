package pl.mj.multicurrencyaccount.domain

import spock.lang.Specification

import java.time.LocalDate

import static pl.mj.multicurrencyaccount.domain.Pesel.*

class PeselSpec extends Specification {

    def 'should return birth date #BIRTH_DATE for pesel #PESEL' () {
        expect:
        new Pesel(PESEL).birthDate() == BIRTH_DATE

        where:
        PESEL           | BIRTH_DATE
        "00010138319"   | LocalDate.of(1900, 1, 1)
        "73030637197"   | LocalDate.of(1973, 3, 6)
        "15271459367"   | LocalDate.of(2015, 7, 14)
    }

    def 'should throw exception when pesel #CASE'() {
        when:
        new Pesel(PESEL)

        then:
        thrown InvalidPeselException

        where:
        CASE                                | PESEL
        "has invalid length"                | "1234"
        "contains chars other than digits"  | "73030637abc"
        "has invalid checksum"              | "15271459363"
    }

}
