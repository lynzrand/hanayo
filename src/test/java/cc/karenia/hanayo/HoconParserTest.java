package cc.karenia.hanayo;

import static org.junit.Assert.*;

import org.junit.*;

public class HoconParserTest {
  @Test
  public void TestKeyParser() throws Exception {
    var charArray = "test.\"quoted\".spaced key".toCharArray();
    var parseResult = HoconParser.parseKey(charArray, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.name, "test");
    assertEquals(parseResult.result.next.name, "quoted");
    assertEquals(parseResult.result.next.next.name, "spaced key");
  }

  @Test
  public void TestNumberParser() throws Exception {
    var number1 = "10000 ".toCharArray();
    var number2 = "10000e ".toCharArray();
    var number3 = "10000e5 ".toCharArray();
    var number4 = "10000.1 ".toCharArray();
    var number5 = "10000.1e5 ".toCharArray();

    var parseResult = HoconParser.parseNumber(number1, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.originalString, "10000");
    assertEquals(parseResult.result.asInt(), 10000);

    parseResult = HoconParser.parseNumber(number2, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.originalString, "10000");

    parseResult = HoconParser.parseNumber(number3, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.originalString, "10000e5");
    assertEquals(parseResult.result.asDouble(), 1000000000d, 1);

    parseResult = HoconParser.parseNumber(number4, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.originalString, "10000.1");
    assertEquals(parseResult.result.asDouble(), 10000.1, 0.001);

    parseResult = HoconParser.parseNumber(number5, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.originalString, "10000.1e5");
    assertEquals(parseResult.result.asDouble(), 1000010000d, 1);

  }
}
