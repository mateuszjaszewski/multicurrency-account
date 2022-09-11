package pl.mj.multicurrencyaccount.domain

import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

import static pl.mj.multicurrencyaccount.domain.Owner.*

class OwnerSpec extends Specification {

    static final Pesel PESEL = new Pesel("90030525437")

    def 'should calculate age as #EXPECTED_AGE when it is #DATE and owner was born on #BIRTH_DATE'() {
        given:
        def owner = new Owner(PESEL, "Jan", "Kowalski")

        expect:
        owner.age(fixedClock(DATE)) == EXPECTED_AGE

        where:
        DATE                        | EXPECTED_AGE
        LocalDate.of(2022, 3, 4)    | 31
        LocalDate.of(2022, 3, 5)    | 32
        LocalDate.of(2022, 3, 6)    | 32
        LocalDate.of(2023, 12, 31)  | 33

        BIRTH_DATE = PESEL.birthDate()
    }

    def 'should not allow for blank first name'() {
        when:
        new Owner(PESEL, " ", "Kowalski")

        then:
        thrown InvalidOwnerDataException
    }

    def 'should not allow for blank last name'() {
        when:
        new Owner(PESEL, "Jan", " ")

        then:
        thrown InvalidOwnerDataException
    }

    private static Clock fixedClock(LocalDate date) {
        def time = LocalTime.of(12, 30, 10)
        def instant = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC)
        return Clock.fixed(instant, ZoneOffset.UTC)
    }

}
