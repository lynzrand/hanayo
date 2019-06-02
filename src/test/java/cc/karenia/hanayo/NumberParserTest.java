package cc.karenia.hanayo;

import static org.junit.Assert.*;
import org.junit.*;

import org.junit.Test;

import cc.karenia.hanayo.types.HoconParseException;

public class NumberParserTest {

  @Test
  public void TestRegularNumber() throws HoconParseException {
    var parseResult = HoconParser.of("10000").parseNumber(0);
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.value, "10000");
    assertEquals(parseResult.result.asInt(), 10000);

    parseResult = HoconParser.of("10000e").parseNumber(0);
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.value, "10000");

    parseResult = HoconParser.of("10000e5").parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "10000e5");
    assertEquals(parseResult.result.asDouble(), 1000000000d, 1);

    parseResult = HoconParser.of("10000.1").parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "10000.1");
    assertEquals(parseResult.result.asDouble(), 10000.1, 0.001);

    parseResult = HoconParser.of("10000.1e5").parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "10000.1e5");
    assertEquals(parseResult.result.asDouble(), 1000010000d, 1);

    parseResult = HoconParser.of("-10000.1e5").parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "-10000.1e5");
    assertEquals(parseResult.result.asDouble(), -1000010000d, 1);
  }

  @Test
  public void testNotNumber() {
    try {
      HoconParser.of("-aaaabbcc").parseNumber(0);
      Assert.fail("Should not parse string preceded with minus sign");
    } catch (HoconParseException e) {
      assertEquals(e.ptr, 1);
    }

    try {
      HoconParser.of("aaaabbcc").parseNumber(0);
      Assert.fail("Should not parse string preceded with minus sign");
    } catch (HoconParseException e) {
      assertEquals(e.ptr, 0);
    }
  }
}
