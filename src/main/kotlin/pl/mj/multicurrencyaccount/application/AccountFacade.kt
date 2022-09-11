package pl.mj.multicurrencyaccount.application

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.mj.multicurrencyaccount.application.AccountController.*
import pl.mj.multicurrencyaccount.domain.*
import pl.mj.multicurrencyaccount.domain.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.Instant

@Component
class AccountFacade(val accountRepository: AccountRepository,
                    val currencyRateProvider: CurrencyRateProvider,
                    val clock: Clock) {

    @Transactional
    fun registerAccount(request: RegisterAccountRequest) {
        if (request.initialDeposit < BigDecimal.ZERO) {
            throw InvalidOperationException("Initial deposit cannot be negative")
        }
        val account = accountRepository.getById(request.owner.pesel)
        val owner = Owner(Pesel(request.owner.pesel), request.owner.firstName, request.owner.lastName)
        val initialDeposit = request.initialDeposit.setScale(2, RoundingMode.DOWN)
        account.register(RegisterAccountCommand(now(), owner, initialDeposit))
        accountRepository.save(account)
    }

    @Transactional
    fun exchangeMoney(pesel: String, request: ExchangeCurrencyRequest) {
        if (request.amount < BigDecimal.ZERO) {
            throw InvalidOperationException("Amount cannot be negative")
        }
        if (request.sourceCurrency != Currency.PLN && request.targetCurrency != Currency.PLN) {
            throw InvalidOperationException("Only exchange from or to PLN is supported")
        }
        if (request.sourceCurrency == request.targetCurrency) {
            throw InvalidOperationException("Source currency and target currency must be different")
        }

        val account = findRegisteredAccount(pesel)
        if (request.sourceCurrency == Currency.PLN) {
            val buyingRate = inverse(currencyRateProvider.buyingRate(request.targetCurrency))
            account.exchangeCurrency(ExchangeCurrencyCommand(
                    now(), request.amount, request.sourceCurrency, request.targetCurrency, buyingRate))
        }
        if (request.targetCurrency == Currency.PLN) {
            val sellingRate = currencyRateProvider.sellingRate(request.sourceCurrency)
            account.exchangeCurrency(ExchangeCurrencyCommand(
                    now(), request.amount, request.sourceCurrency, request.targetCurrency, sellingRate))
        }
        accountRepository.save(account)
    }

    @Transactional(readOnly = true)
    fun accountDetails(pesel: String): AccountDetailsResponse {
        val account = findRegisteredAccount(pesel)
        val owner = account.owner
        return AccountDetailsResponse(
                OwnerDto(owner.pesel.value, owner.firstName, owner.lastName),
                account.subAccounts.map { SubAccountDto(it.currency, it.balance) }
        )
    }

    @Transactional(readOnly = true)
    fun accountTransactions(pesel: String): AccountTransactionsResponse {
        val account = findRegisteredAccount(pesel)
        val transactions = account.events.sortedWith(compareBy(DomainEvent::timestamp).reversed()).map { event ->
            when (event) {
                is AccountRegisteredEvent -> InitialDepositDto(event.timestamp, event.initialDeposit)
                is CurrencyExchangedEvent -> CurrencyExchangedDto(event.timestamp, event.amount,
                        event.sourceCurrency, event.targetCurrency, event.rate)
            }
        }
        return AccountTransactionsResponse(transactions)
    }

    private fun findRegisteredAccount(pesel: String): Account {
        val account = accountRepository.getById(pesel)
        if (!account.isRegistered()) {
            throw AccountNotFoundException()
        }
        return account
    }

    private fun now() = Instant.now(clock)

    private fun inverse(rate: BigDecimal): BigDecimal {
        return BigDecimal.ONE.divide(rate, 4, RoundingMode.HALF_UP)
    }

    class AccountNotFoundException : RuntimeException("Cannot find registered account")
    class InvalidOperationException(override val message: String) : RuntimeException(message)

}