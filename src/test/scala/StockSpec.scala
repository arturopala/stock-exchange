import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest.prop.PropertyChecks
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.math.BigDecimal;
import me.arturopala.stockexchange.api._
import me.arturopala.stockexchange.util.Money
import me.arturopala.stockexchange.stock._

class StockSpec extends WordSpecLike with Matchers with PropertyChecks {

  "A CommonStock" should {

    "calculate dividend yield" in {
      val stock = new CommonStock("TEST", new Money(100), new Money(10))
      stock.calculateDividendYield(Money.parse("2")) should be(5d)
    }

    "calculate P/E ratio" in {
      val stock = new CommonStock("TEST", new Money(100), new Money(10))
      stock.calculatePERatio(Money.parse("2")) should be(0.2)
    }

    "not calculate P/E ratio if dividend is zero" in {
      val stock = new CommonStock("TEST", new Money(100), new Money(0))
      java.lang.Double.isNaN(stock.calculatePERatio(Money.parse("2"))) should be(true)
    }

  }

  "A PreferredStock" should {

    "calculate dividend yield" in {
      val stock = new PreferredStock("TEST", new Money(100), new Money(10), new BigDecimal("0.02"))
      stock.calculateDividendYield(Money.parse("2")) should be(1d)
    }

    "calculate P/E ratio" in {
      val stock = new PreferredStock("TEST", new Money(100), new Money(10), new BigDecimal("0.02"))
      stock.calculatePERatio(Money.parse("2")) should be(0.2)
    }

    "not calculate P/E ratio if dividend is zero" in {
      val stock = new PreferredStock("TEST", new Money(100), Money.ZERO, new BigDecimal("0.02"))
      java.lang.Double.isNaN(stock.calculatePERatio(Money.parse("2"))) should be(true)
    }

  }

}