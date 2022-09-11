# Multi-currency account

Aplikacja umożlwia dokonywanie wymiany walut PLN <-> USD po aktualnym kursie pobranym z https://api.nbp.pl/api/exchangerates/rates/c/ .
Założone zostało że główną walutą na konice jest PLN i w zależności od tego czy dokonujemy wymiany z czy na złotówki to transakcja 
traktowana jest jako sprzedaż lub kupno obcej waluty - ma to wpływ na kurs jaki jest przyjmowany dla transakcji.

## Architektura
Aplikacja powstała w języku Kotlin przy wykorzystaniu DDD oraz event-sourcing'u, oczywiście ze względu na dość skromną domenę 
takie podejście jest w tym przypadku "overengineering'iem" ale miało to bardziej na celu pokazanie jak taka implmentacja mogła by wyglądać. 

## Wymagania
- Java w wesji co najmniej 1.8
- wolny port 8080

## Uruchomienie
Aplikację można uruchomić poprzez wykonanie polecenia `./gradlew bootRun` lub wywołując metodę main z klasy `MultiCurrencyAccountApp`. 

## Dokumentacja API
Dokumentację API jakie udostępnia aplikacja można znaleźć po uruchomieniu pod adresem: http://localhost:8080/swagger-ui/index.html#/ .

## Testy
Testy jednostkowe oraz test integracyjny zostały przygotowane z wykorzystaniem języka Groovy i framework'a Spock.
Test integracyjny `AccountIntegrationSpec` został oznaczony jako `@Stepwise` ponieważ powinien być wykonywany w sposób sekwncyjny.
W tym teście najpierw przechodzony jest pozytywny przypadek, czyli: 
- rejestracja konta,
- wymiana PLN na USD,
- wymiana USD na PLN,
- pobranie stanu kontakt,
- pobranie listy transakcji wykonanych na koncie.

Potem wykonywane jest kilka żądań, które kończą się błędemami.