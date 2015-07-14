import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest.prop.PropertyChecks
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.math.BigDecimal;
import me.arturopala.stockexchange.api._
import me.arturopala.stockexchange.util.Money;

class MoneySpec extends WordSpecLike with Matchers with PropertyChecks {

  "A Money" should {

    "can be created from zero BigDecimal" in {
      val money = new Money(BigDecimal.ZERO)
      money should be(Money.ZERO)
      money.toString should be("-")
    }

    "can be created from any positive BigDecimal" in {
      val money = new Money(new BigDecimal("16678.89888"))
      money.toString should be("16678.8989")
    }

    "can be created from any positive integer" in {
      val money = new Money(166261762)
      money.toString should be("166261762")
    }

    "can be parsed from valid string" in {
      val money = Money.parse("  738743.78991")
      money.toString should be("738743.7899")
    }

    "return zero if parsed from invalid string" in {
      val money = Money.parse("  - ")
      money.toString should be("-")
    }

    "have isDefined check" in {
      val money = Money.parse("10.19")
      money.isDefined() should be(true)
      val zero = Money.ZERO
      zero.isDefined() should be(false)
      val undefined = Money.UNDEFINED
      undefined.isDefined() should be(false)
    }

    "add to another Money" in {
      val money = Money.parse("12.3456").add(Money.parse("5.8942"))
      money.toString should be("18.2398")
    }

    "subtract another Money" in {
      val money = Money.parse("12.3456").subtract(Money.parse("5.8942"))
      money.toString should be("6.4514")
    }

    "divide by another Money" in {
      val decimal = Money.parse("12.3456").divide(Money.parse("5.8942"))
      decimal.toString should be("2.094534")
    }

    "divide by positive decimal" in {
      val money = Money.parse("12.3456").divide(new BigDecimal("5.8942"))
      money should be(Money.parse("2.0945"))
    }

    "not divide by negative decimal" in {
      an[IllegalArgumentException] should be thrownBy Money.parse("12.3456").divide(new BigDecimal("-5.8942"))
    }

    "divide by zero and return UNKNOWN" in {
      Money.parse("12.3456").divide(new BigDecimal("0")) should be(Money.UNDEFINED)
    }

    "multiply by decimal" in {
      val money = Money.parse("12.3456").multiply(new BigDecimal("5.8942"))
      money should be(Money.parse("72.7674"))
    }

    "not be created from null" in {
      an[IllegalArgumentException] should be thrownBy new Money(null)
    }

    "not be created from negative value" in {
      an[IllegalArgumentException] should be thrownBy new Money(new BigDecimal("-0.001"))
    }

    "have any operation on UNDEFINED return UNDEFINED" in {
      java.lang.Double.isNaN(Money.UNDEFINED.divide(Money.UNDEFINED)) should be(true)
      java.lang.Double.isNaN(Money.UNDEFINED.divide(Money.parse("12.7895"))) should be(true)
      java.lang.Double.isNaN(Money.parse("12.7895").divide(Money.UNDEFINED)) should be(true)
      Money.UNDEFINED.multiply(5) should be(Money.UNDEFINED)
      Money.UNDEFINED.multiply(new BigDecimal("12.785")) should be(Money.UNDEFINED)
      Money.UNDEFINED.divide(5) should be(Money.UNDEFINED)
      Money.UNDEFINED.divide(new BigDecimal("12.785")) should be(Money.UNDEFINED)
      Money.UNDEFINED.add(Money.UNDEFINED) should be(Money.UNDEFINED)
      Money.UNDEFINED.add(Money.parse("12.7895")) should be(Money.UNDEFINED)
      Money.parse("12.7895").add(Money.UNDEFINED) should be(Money.UNDEFINED)
      Money.UNDEFINED.subtract(Money.UNDEFINED) should be(Money.UNDEFINED)
      Money.UNDEFINED.subtract(Money.parse("12.7895")) should be(Money.UNDEFINED)
      Money.parse("12.7895").subtract(Money.UNDEFINED) should be(Money.UNDEFINED)
    }
  }

}