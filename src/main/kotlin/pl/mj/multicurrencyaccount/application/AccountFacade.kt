package pl.mj.multicurrencyaccount.application

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.mj.multicurrencyaccount.application.AccountController.*
import pl.mj.multicurrencyaccount.domain.*
import pl.mj.multicurrencyaccount.domain.Currency
import java.time.Clock
import java.time.Instant

@Component
class AccountFacade(val accountRepository: AccountRepository,
                    val currencyRateProvider: CurrencyRateProvider,
                    val clock: Clock) {

    @Transactional
    fun registerAccount(request: RegisterAccountRequest) {
        val account = accountRepository.getById(request.owner.pesel)
        val owner = Owner(Pesel(request.owner.pesel), request.owner.firstName, request.owner.lastName)
        account.register(RegisterAccountCommand(now(), owner, request.initialDeposit))
        accountRepository.save(account)
    }

    @Transactional
    fun exchangeMoney(pesel: String, request: MoneyExchangeRequest) {
        if (request.sourceCurrency != Currency.PLN && request.targetCurrency != Currency.PLN) {
            throw InvalidOperationException("Only exchange from or to PLN is supported")
        }
        if (request.sourceCurrency == request.targetCurrency) {
            throw InvalidOperationException("Source currency and target currency must be different")
        }

        val account = accountRepository.getById(pesel)
        if (request.sourceCurrency == Currency.PLN) {
            val buyingRate = currencyRateProvider.buyingRate(request.targetCurrency)
            account.buyCurrency(BuyCurrencyCommand(now(), request.amount, request.targetCurrency, buyingRate))
        }
        if (request.targetCurrency == Currency.PLN) {
            val sellingRate = currencyRateProvider.sellingRate(request.sourceCurrency)
            account.sellCurrency(SellCurrencyCommand(now(), request.amount, request.sourceCurrency, sellingRate))
        }
        accountRepository.save(account)
    }

    @Transactional(readOnly = true)
    fun accountDetails(pesel: String): AccountDetailsResponse {
        val account = findRegisteredAccount(pesel)
        val owner = account.owner
        return AccountDetailsResponse(
                OwnerDto(owner.pesel.toString(), owner.firstName, owner.lastName),
                account.subAccounts.map { SubAccountDto(it.currency, it.balance) }
        )
    }

    @Transactional(readOnly = true)
    fun accountTransactions(pesel: String): AccountTransactionsResponse {
        val account = findRegisteredAccount(pesel)
        val transactions = account.events.sortedWith(compareBy(DomainEvent::timestamp).reversed()).map { event ->
            when (event) {
                is AccountRegisteredEvent -> InitialDepositDto(event.timestamp, event.initialDeposit)
                is CurrencySoldEvent -> CurrencySoldDto(event.timestamp, event.currency, event.amount, event.rate)
                is CurrencyBoughtEvent -> CurrencyBoughtDto(event.timestamp, event.currency, event.amount, event.rate)
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

    class AccountNotFoundException : RuntimeException("Cannot find registered account")
    class InvalidOperationException(override val message: String) : RuntimeException(message)

}