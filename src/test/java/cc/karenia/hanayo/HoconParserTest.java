package cc.karenia.hanayo;

import static org.junit.Assert.*;

import org.junit.*;

import cc.karenia.hanayo.types.HoconParseException;

public class HoconParserTest {
  static {
    HoconParseException.shouldGatherStacktrace = true;
  }

  @Test
  public void TestKeyParser() throws HoconParseException {
    var str = "test.\"quoted\".spaced key";
    var parseResult = HoconParser.of(str).parseKey(0);
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.name, "test");
    assertEquals(parseResult.result.next.name, "quoted");
    assertEquals(parseResult.result.next.next.name, "spaced key");
  }

  @Test
  public void TestNumberParser() throws HoconParseException {
    var number1 = "10000 ";
    var number2 = "10000e ";
    var number3 = "10000e5 ";
    var number4 = "10000.1 ";
    var number5 = "10000.1e5 ";

    var parseResult = HoconParser.of(number1).parseNumber(0);
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.value, "10000");
    assertEquals(parseResult.result.asInt(), 10000);

    parseResult = HoconParser.of(number2).parseNumber(0);
    assertEquals(parseResult.result.isInteger, true);
    assertEquals(parseResult.result.value, "10000");

    parseResult = HoconParser.of(number3).parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "10000e5");
    assertEquals(parseResult.result.asDouble(), 1000000000d, 1);

    parseResult = HoconParser.of(number4).parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "10000.1");
    assertEquals(parseResult.result.asDouble(), 10000.1, 0.001);

    parseResult = HoconParser.of(number5).parseNumber(0);
    assertEquals(parseResult.result.isInteger, false);
    assertEquals(parseResult.result.value, "10000.1e5");
    assertEquals(parseResult.result.asDouble(), 1000010000d, 1);

  }

  @Test
  public void testSubstitution() throws HoconParseException {
    var sub1 = "${a.c}";
    var parseResult = HoconParser.of(sub1).parseSubstitution(0);
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.isDetermined, true);
    assertEquals(parseResult.result.path.name, "a");
    assertEquals(parseResult.result.path.next.name, "c");

    var sub2 = "${?b.c}";
    parseResult = HoconParser.of(sub2).parseSubstitution(0);
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.isDetermined, false);
    assertEquals(parseResult.result.path.name, "b");
    assertEquals(parseResult.result.path.next.name, "c");
  }

  @Test
  public void testParseValueStringConcatenation() throws Throwable {
    var valueTest = "-123abc";
    var parseResult = HoconParser.of(valueTest).parseValueSegment(0, null, null)
        .unwrapThrow();
    var valueParseResult = HoconParser.of(valueTest).parseValue(0, null, null)
        .unwrapThrow();

    assertEquals(parseResult.asString(), "-123");
    assertEquals(valueParseResult.asString(), "-123abc");

  }
}
