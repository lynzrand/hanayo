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
    var parseResult = HoconParser.of(str).parseKey(0).unwrap();
    assertEquals(parseResult.name, "test");
    assertEquals(parseResult.next.name, "quoted");
    assertEquals(parseResult.next.next.name, "spaced key");
  }

  @Test
  public void testSubstitution() throws HoconParseException {
    var sub1 = "${a.c}";
    var parseResult = HoconParser.of(sub1).parseSubstitution(0).unwrap();
    assertEquals(parseResult.isDetermined, true);
    assertEquals(parseResult.path.name, "a");
    assertEquals(parseResult.path.next.name, "c");

    var sub2 = "${?b.c}";
    parseResult = HoconParser.of(sub2).parseSubstitution(0).unwrap();
    assertEquals(parseResult.isDetermined, false);
    assertEquals(parseResult.path.name, "b");
    assertEquals(parseResult.path.next.name, "c");
  }

  @Test
  public void testParseValueStringConcatenation() throws Throwable {
    var valueTest = "-123abc";
    var parseResult = HoconParser.of(valueTest).parseValueSegment(0, null)
        .unwrapThrow();
    var valueParseResult = HoconParser.of(valueTest).parseValue(0, null)
        .unwrapThrow();

    assertEquals(parseResult.asString(), "-123");
    assertEquals(valueParseResult.asString(), "-123abc");

  }
}
