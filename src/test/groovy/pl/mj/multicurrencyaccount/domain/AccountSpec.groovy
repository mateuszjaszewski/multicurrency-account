package pl.mj.multicurrencyaccount.domain

import spock.lang.Specification

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

import static pl.mj.multicurrencyaccount.domain.Account.*
import static pl.mj.multicurrencyaccount.domain.Currency.*

class AccountSpec extends Specification {

    def id = "1"
    def now = LocalDateTime.of(2022, 9, 11, 12, 00).toInstant(ZoneOffset.UTC)
    def clock = Clock.fixed(now, ZoneOffset.UTC)

    def owner = new Owner(new Pesel("94051564529"), "Jan", "Kowalski")
    def ownerUnderage = new Owner(new Pesel("10210175788"), "Adam", "Nowak")

    def 'should register account for adult owner and set initial deposit in PLN'() {
        given:
        def account = newAccount()
        def initialDeposit = 999.00

        when:
        account.register(new RegisterAccountCommand(now, owner, initialDeposit))

        then:
        account.isRegistered()
        account.owner == owner
        account.balance(PLN) == initialDeposit
        account.pendingEvents.contains(new AccountRegisteredEvent(now, owner, initialDeposit))
    }

    def 'should not allow to register account when owner is not at least 18 years old'() {
        given:
        def account = newAccount()

        when:
        account.register(new RegisterAccountCommand(now, ownerUnderage, 0.00))

        then:
        thrown(CannotRegisterAccountException)
    }

    def 'should not allow to register account when it is already registered'() {
        given:
        def account = registeredAccount()

        when:
        account.register(new RegisterAccountCommand(now, owner, 0.00))

        then:
        thrown(CannotRegisterAccountException)
    }

    def 'should exchange PLN to USD when there is enough money on PLN sub-account'() {
        given:
        def account = registeredAccount(1000.00)
        def command = new ExchangeCurrencyCommand(now, 500.00, PLN, USD, 0.2)

        when:
        account.exchangeCurrency(command)

        then:
        account.balance(PLN) == 500.00
        account.balance(USD) == 100.00
        account.pendingEvents.contains(new CurrencyExchangedEvent(now, command.amount, PLN, USD, command.rate))
    }

    def 'should exchange USD to PLN when there is enough money on USD sub-account'() {
        given:
        def account = registeredAccount(1000.00)
        account.exchangeCurrency(new ExchangeCurrencyCommand(now, 500.00, PLN, USD, 0.2))

        and:
        def command = new ExchangeCurrencyCommand(now, 50.00, USD, PLN, 4.00)

        when:
        account.exchangeCurrency(command)

        then:
        account.balance(PLN) == 700.00
        account.balance(USD) == 50.00
        account.pendingEvents.contains(new CurrencyExchangedEvent(now, command.amount, USD, PLN, command.rate))
    }

    def 'should not allow to exchange PLN to USD when there is not enough money on PLN sub-account'() {
        given:
        def account = registeredAccount(10.00)

        when:
        account.exchangeCurrency(new ExchangeCurrencyCommand(now, 100.00, PLN, USD, 0.2))

        then:
        thrown(InsufficientFoundsException)
    }


    def 'should not allow to exchange USD to PLN when there is not enough money on USD sub-account'() {
        given:
        def account = registeredAccount(50.00)
        account.exchangeCurrency(new ExchangeCurrencyCommand(now, 50.00, PLN, USD, 0.2))

        when:
        account.exchangeCurrency(new ExchangeCurrencyCommand(now, 100.00, USD, PLN, 4.00))

        then:
        thrown(InsufficientFoundsException)
    }

    private Account newAccount() {
        return new Account(id, [], clock)
    }

    private Account registeredAccount(BigDecimal initialDeposit = 0) {
        def account = new Account(id, [], clock)
        account.register(new RegisterAccountCommand(now, owner, initialDeposit))
        return account
    }

}
