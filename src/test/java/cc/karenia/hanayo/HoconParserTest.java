package cc.karenia.hanayo;

import static org.junit.Assert.*;

import org.junit.*;

import cc.karenia.hanayo.types.HoconParseException;

public class HoconParserTest {
  @Test
  public void TestKeyParser() throws HoconParseException {
    var charArray = "test.\"quoted\".spaced key".toCharArray();
    var parseResult = HoconParser.parseKey(charArray, 0);
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.name, "test");
    assertEquals(parseResult.result.next.name, "quoted");
    assertEquals(parseResult.result.next.next.name, "spaced key");
  }

  @Test
  public void TestNumberParser() throws HoconParseException {
    var number1 = "10000 ".toCharArray();
    var number2 = "10000e ".toCharArray();
    var number3 = "10000e5 ".toCharArray();
    var number4 = "10000.1 ".toCharArray();
    var number5 = "10000.1e5 ".toCharArray();

    var parseResult = HoconParser.parseNumber(number1, 0);
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.originalString, "10000");
    assertEquals(parseResult.result.asInt(), 10000);

    parseResult = HoconParser.parseNumber(number2, 0);
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.originalString, "10000");

    parseResult = HoconParser.parseNumber(number3, 0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.originalString, "10000e5");
    assertEquals(parseResult.result.asDouble(), 1000000000d, 1);

    parseResult = HoconParser.parseNumber(number4, 0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.originalString, "10000.1");
    assertEquals(parseResult.result.asDouble(), 10000.1, 0.001);

    parseResult = HoconParser.parseNumber(number5, 0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.originalString, "10000.1e5");
    assertEquals(parseResult.result.asDouble(), 1000010000d, 1);

  }

  @Test
  public void testSubstitution() throws HoconParseException {
    var sub1 = "${a.c}".toCharArray();
    var parseResult = HoconParser.parseSubstitution(sub1, 0);
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.isDetermined, true);
    assertEquals(parseResult.result.path.name, "a");
    assertEquals(parseResult.result.path.next.name, "c");

    var sub2 = "${?b.c}".toCharArray();
    parseResult = HoconParser.parseSubstitution(sub2, 0);
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.isDetermined, false);
    assertEquals(parseResult.result.path.name, "b");
    assertEquals(parseResult.result.path.next.name, "c");
  }

  @Test
  public void testParseValueStringConcatenation() throws Throwable {
    var valueTest = "-123abc".toCharArray();
    var parseResult = HoconParser.parseValueSegment(valueTest, 0, null, null).unwrapThrow();

    assertEquals(parseResult.asString(), "-123abc");

  }
}
