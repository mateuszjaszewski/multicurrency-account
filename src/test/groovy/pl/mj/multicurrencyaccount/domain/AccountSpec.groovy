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

    def 'should buy USD when there is enough money on PLN sub-account'() {
        given:
        def account = registeredAccount(1000.00)
        def buyCommand = new BuyCurrencyCommand(now, 100.00, USD, 4.00)

        when:
        account.buyCurrency(buyCommand)

        then:
        account.balance(PLN) == 600.00
        account.balance(USD) == 100.00
        account.pendingEvents.contains(new CurrencyBoughtEvent(now, USD, buyCommand.amount, buyCommand.rate))
    }

    def 'should sell USD when there is enough money on USD sub-account'() {
        given:
        def account = registeredAccount(1000.00)
        account.buyCurrency(new BuyCurrencyCommand(now, 100.00, USD, 4.00))

        and:
        def sellCommand = new SellCurrencyCommand(now, 50.00, USD, 3.00)

        when:
        account.sellCurrency(sellCommand)

        then:
        account.balance(PLN) == 750.00
        account.balance(USD) == 50.00
        account.pendingEvents.contains(new CurrencySoldEvent(now, USD, sellCommand.amount, sellCommand.rate))
    }

    def 'should not allow to buy USD when there is not enough money on PLN sub-account'() {
        given:
        def account = registeredAccount(10.00)

        when:
        account.buyCurrency(new BuyCurrencyCommand(now, 100.00, USD, 4.00))

        then:
        thrown(InsufficientFoundsException)
    }


    def 'should not allow to sell USD when there is not enough money on USD sub-account'() {
        given:
        def account = registeredAccount(40.00)
        account.buyCurrency(new BuyCurrencyCommand(now, 10.00, USD, 4.00))

        when:
        account.sellCurrency(new SellCurrencyCommand(now, 100.00, USD, 4.00))

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
