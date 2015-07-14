import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest.prop.PropertyChecks
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.math.BigDecimal;
import me.arturopala.stockexchange.api._
import me.arturopala.stockexchange.stock._
import me.arturopala.stockexchange.util.Money
import collection.JavaConversions._
import me.arturopala.stockexchange.gbce.GBCE
import me.arturopala.stockexchange.simpleimpl._
import java.time.Clock
import java.util.concurrent.atomic.AtomicReference

class StockExchangeSpec extends WordSpecLike with Matchers with PropertyChecks {

  val stock = new CommonStock("COMM", new Money(100), new Money(10))
  val stock2 = new PreferredStock("PREF", new Money(100), new Money(10), new BigDecimal("0.2"))

  "A GBCE factory" should {

    "initialize GBCE listing from text file" in {
      val listing = GBCE.LISTING
      listing should not be empty
      listing should contain(new CommonStock("ALE", new Money(60), new Money(23)))
      listing should contain(new PreferredStock("GIN", new Money(100), new Money(8), new BigDecimal("0.02")))
      listing should contain(new CommonStock("JOE", new Money(250), new Money(13)))
      listing should contain(new CommonStock("TEA", new Money(100), new Money(0)))
      listing should contain(new CommonStock("POP", new Money(100), new Money(0)))
    }

    "provide instance of simple stock exchange" in {
      val exchange = GBCE.simpleStockExchange()
      exchange.listing().toSet should contain theSameElementsAs GBCE.LISTING
    }
  }

  "A Trade" should {

    "be created with timestamp, tradeType, stock, quantity and price" in {
      val timestamp = Instant.now()
      val tradeType = TradeType.SELL
      val quantity = 178
      val price = new Money(1564)
      val trade = new Trade(timestamp, tradeType, stock, quantity, price)
      trade.timestamp should be(timestamp)
      trade.`type` should be(tradeType)
      trade.stock should be(stock)
      trade.quantity should be(quantity)
      trade.price should be(price)
    }

    "have comparator by timestamp" in {
      val quantity = 178
      val price = new Money(1564)
      val trade1 = new Trade(Instant.now().minusSeconds(20), TradeType.SELL, stock, quantity, price)
      val trade2 = new Trade(Instant.now().minusSeconds(10), TradeType.SELL, stock, quantity, price)
      val trade3 = new Trade(Instant.now().minusSeconds(10), TradeType.BUY, stock, quantity, price)
      val comparator = Trade.BY_TIME_COMPARATOR
      comparator.compare(trade1, trade2) should be < 0
      comparator.compare(trade2, trade1) should be > 0
      comparator.compare(trade1, trade1) should be(0)
      comparator.compare(trade2, trade2) should be(0)
      comparator.compare(trade2, trade3) should be(0)
      comparator.compare(trade3, trade2) should be(0)
    }

  }

  "A TickerActor" should {

    import akka.actor._
    import akka.testkit._

    "be created with stock and stockInfoRef" in {
      val system = ActorSystem()
      val stockInfoRef = new AtomicReference[StockInfo](new StockInfo())
      val props = Props(classOf[TickerActor], stock, stockInfoRef)
      val actor = system.actorOf(props)
      system.shutdown()
    }

    "receive Trade message and calculate current price" in {
      implicit val system = ActorSystem()
      val stockInfoRef = new AtomicReference[StockInfo](new StockInfo())
      val props = Props(classOf[TickerActor], stock2, stockInfoRef)
      val actor = TestActorRef(props)
      actor ! new Trade(Instant.now(), TradeType.SELL, stock2, 100, new Money(10))
      stockInfoRef.get.price should be(new Money(10))
      stockInfoRef.get.quantity should be(100)
      stockInfoRef.get.volume should be(new Money(1000))
      actor ! new Trade(Instant.now(), TradeType.BUY, stock, 110, new Money(20))
      stockInfoRef.get.price should be(Money.parse("15.2381"))
      stockInfoRef.get.quantity should be(210)
      stockInfoRef.get.volume should be(new Money(3200))
      actor ! new Trade(Instant.now(), TradeType.SELL, stock, 120, new Money(30))
      stockInfoRef.get.price should be(Money.parse("20.6061"))
      actor ! new Trade(Instant.now(), TradeType.SELL, stock2, 130, new Money(40))
      stockInfoRef.get.price should be(Money.parse("26.087"))
      system.shutdown()
    }

    "receive Tick message and clean queue" in {
      implicit val system = ActorSystem()
      val stockInfoRef = new AtomicReference[StockInfo](new StockInfo())
      val props = Props(classOf[TickerActor], stock, stockInfoRef)
      val actor = TestActorRef(props)
      val now = Instant.now()
      actor ! new Trade(now.minusSeconds(60), TradeType.SELL, stock2, 100, new Money(10))
      actor ! new Trade(now.minusSeconds(40), TradeType.BUY, stock2, 110, new Money(20))
      actor ! new Trade(now.minusSeconds(20), TradeType.SELL, stock, 120, new Money(30))
      actor ! new Trade(now, TradeType.SELL, stock, 130, new Money(40))
      stockInfoRef.get.price should be(Money.parse("26.087"))
      actor ! new Tick(now.minusSeconds(50))
      stockInfoRef.get.price should be(Money.parse("30.5556"))
      actor ! new Tick(now.minusSeconds(30))
      stockInfoRef.get.price should be(Money.parse("35.2"))
      actor ! new Tick(now.minusSeconds(10))
      stockInfoRef.get.price should be(Money.parse("40"))
      system.shutdown()
    }

    "receive Trade and Tick messages in any order" in {
      implicit val system = ActorSystem()
      val stockInfoRef = new AtomicReference[StockInfo](new StockInfo())
      val props = Props(classOf[TickerActor], stock, stockInfoRef)
      val actor = TestActorRef(props)
      val now = Instant.now()
      actor ! new Trade(now.minusSeconds(60), TradeType.SELL, stock, 100, new Money(10))
      stockInfoRef.get.price should be(Money.parse("10"))
      actor ! new Trade(now.minusSeconds(40), TradeType.BUY, stock2, 1210, Money.parse("20.12"))
      stockInfoRef.get.price should be(Money.parse("19.3475"))
      actor ! new Tick(now.minusSeconds(50))
      stockInfoRef.get.price should be(Money.parse("20.12"))
      actor ! new Tick(now.minusSeconds(41))
      actor ! new Trade(now.minusSeconds(20), TradeType.BUY, stock2, 120, Money.parse("30.2"))
      actor ! new Tick(now.minusSeconds(21))
      stockInfoRef.get.price should be(Money.parse("30.2"))
      actor ! new Tick(now.minusSeconds(21))
      stockInfoRef.get.price should be(Money.parse("30.2"))
      actor ! new Trade(now, TradeType.SELL, stock, 130, Money.parse("40.1"))
      stockInfoRef.get.price should be(Money.parse("35.348"))
      system.shutdown()
    }

  }

  "A SimpleStockExchange" should {

    "have to be initialized with stock listing" in {
      val listing = Set[Stock](stock, stock2)
      val exchange = new SimpleStockExchange(listing)
      exchange.listing.toSet should have size 2
      exchange.listing.toSet should contain only (stock, stock2)
    }

    "find stock if exists" in {
      val listing = Set[Stock](stock, stock2)
      val exchange = new SimpleStockExchange(listing)
      val found2 = exchange.find("PREF")
      found2.isPresent should be(true)
      found2.get should be(stock2)
      val found1 = exchange.find("COMM")
      found1.isPresent should be(true)
      found1.get should be(stock)
    }

    "not find stock if not exists" in {
      val listing = Set[Stock](stock, stock2)
      val exchange = new SimpleStockExchange(listing)
      val found2 = exchange.find("PRE")
      found2.isPresent should be(false)
      val found1 = exchange.find("COM")
      found1.isPresent should be(false)
    }

    "open and close exchange" in {
      val listing = Set[Stock](stock, stock2)
      val exchange = new SimpleStockExchange(listing)
      exchange.isOpen should be(false)
      exchange.open()
      exchange.isOpen should be(true)
      exchange.close()
      exchange.isOpen should be(false)
    }

    "allow to sell, buy and watch stocks when open" in {
      val listing = Set[Stock](stock, stock2)
      val exchange = new SimpleStockExchange(listing).open()
      val ticker1 = exchange.watch(stock)
      val ticker2 = exchange.watch(stock2)
      for (i <- 1 to 10) {
        exchange.sell(stock, 10 - i, new Money(10 + i))
        exchange.buy(stock2, 10 + i, new Money(10 - i))
      }
      Thread.sleep(1000);
      ticker1.price() should be(Money.parse("13.6667"))
      ticker2.price() should be(Money.parse("4.5556"))
      exchange.close()
    }

    "do not allow to sell or buy when closed" in {
      val listing = Set[Stock](stock, stock2)
      val exchange = new SimpleStockExchange(listing)
      an[StockExchangeClosedException] should be thrownBy exchange.sell(stock, 15, new Money(20))
      an[StockExchangeClosedException] should be thrownBy exchange.buy(stock2, 15, new Money(20))
    }

    "calculate all share index" in {
      val stock3 = new CommonStock("EXT", new Money(18), new Money(5))
      val listing = Set[Stock](stock, stock2, stock3)
      val exchange = new SimpleStockExchange(listing).open()
      val now = Instant.now()
      exchange.sell(stock, 15, new Money(20))
      exchange.buy(stock2, 100, new Money(7))
      val ticker1 = exchange.watch(stock)
      val ticker2 = exchange.watch(stock2)
      Thread.sleep(1000);
      ticker1.price() should be(Money.parse("20"))
      ticker2.price() should be(Money.parse("7"))
      exchange.asInstanceOf[SimpleStockExchange].calculateAllShareIndex() should be(11.832159566199232d)
      exchange.close()
    }

  }

}