import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest.prop.PropertyChecks
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.math.BigDecimal;
import me.arturopala.stockexchange.api._
import me.arturopala.stockexchange.util.ParseUtils
import me.arturopala.stockexchange.util.Money
import me.arturopala.stockexchange.stock._

class ParseUtilsSpec extends WordSpecLike with Matchers with PropertyChecks {

  "A ParseUitls" should {

    "split text into array of values" in {
      val text = "MeB, 78.1/90/ Abc;    14.123456 		Com"
      val array = ParseUtils.split(text, ParseUtils.VALUE_REGEX)
      array should be(Array("MeB", "78.1", "90", "Abc", "14.123456", "Com"))
    }

    "parse valid common stock definition to CommonStock instance" in {
      val text = "ALE, Common, 23; -, 60"
      val stock = ParseUtils.STOCK_PARSER(text)
      val expected = new CommonStock("ALE", new Money(60), new Money(23))
      stock should be(expected)
      stock.symbol should be(expected.symbol)
      stock.`type` should be(StockType.COMMON)
      stock.parValue() should be(expected.parValue)
    }

    "parse valid preferred stock definition to PreferredStock instance" in {
      val text = "GIN;Preferred;8;2;100;"
      val stock = ParseUtils.STOCK_PARSER(text)
      val expected = new PreferredStock("GIN", new Money(100), new Money(8), new BigDecimal(2))
      stock should be(expected)
      stock.symbol should be(expected.symbol)
      stock.`type` should be(StockType.PREFERRED)
      stock.parValue() should be(expected.parValue)
    }

    "parse valid string to BigDecimal" in {
      val text = "    1568.12229 "
      val decimal = ParseUtils.parseDecimal(text, BigDecimal.ZERO)
      decimal should be(new BigDecimal("1568.12229"))
    }

    "parse dash as zero" in {
      val text = "    - "
      val decimal = ParseUtils.parseDecimal(text, BigDecimal.ONE)
      decimal should be(BigDecimal.ZERO)
    }

    "parse percents" in {
      val text = "    2% "
      val decimal = ParseUtils.parseDecimal(text, BigDecimal.ONE)
      decimal should be(new BigDecimal("0.02"))
    }

    "try to parse invalid string to BigDecimal" in {
      val text = "    (12.34!) "
      val decimal = ParseUtils.parseDecimal(text, new BigDecimal("18.333"))
      decimal should be(new BigDecimal("12.34"))
    }

    "try to parse invalid string to BigDecimal and return default value" in {
      val text = "    asasasa "
      val decimal = ParseUtils.parseDecimal(text, new BigDecimal("18.333"))
      decimal should be(new BigDecimal("18.333"))
    }
  }

}