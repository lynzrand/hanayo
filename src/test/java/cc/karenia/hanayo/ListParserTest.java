package cc.karenia.hanayo;

import org.junit.*;

import cc.karenia.hanayo.types.*;
import static org.junit.Assert.*;

public class ListParserTest {
  static {
    HoconParseException.shouldGatherStacktrace = true;
  }

  @Test
  public void testParseCommaSeparatedList() throws HoconParseException {
    var result = HoconParser.of("[ 1, 2, \"three\" ]").parseList(0, null)
        .unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(element0, new HoconNumber("1", true));
    var element1 = result.get(1);
    assertEquals(element1, new HoconNumber("2", true));
    var element2 = result.get(2);
    assertEquals(element2, new HoconString("three"));
  }

  @Test
  public void testParseEolSeparatedList() throws HoconParseException {
    var result = HoconParser.of("[\r\n  1\r\n  3\r\n  five\r\n]")
        .parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(element0, new HoconNumber("1", true));
    var element1 = result.get(1);
    assertEquals(element1, new HoconNumber("3", true));
    var element2 = result.get(2);
    assertEquals(element2, new HoconString("five", false, false));
  }

  @Test
  public void testParseMixedSeparatorList() throws HoconParseException {
    var result = HoconParser.of("[\r\n  1, 2,\r\n  3, 4,\r\n  five, six\r\n]")
        .parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(element0, new HoconNumber("1", true));
    var element1 = result.get(1);
    assertEquals(element1, new HoconNumber("2", true));
    var element4 = result.get(4);
    assertEquals(element4, new HoconString("five", false, false));
    var element5 = result.get(5);
    assertEquals(element5, new HoconString("six", false, false));
  }

  @Test
  public void testParseCommentEnabledList() throws HoconParseException {
    var result = HoconParser.of(
        "[\r\n  1 // this is the first element\r\n  3 # this is the second element\r\n  five\r\n]")
        .parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(element0, new HoconNumber("1", true));
    var element1 = result.get(1);
    assertEquals(element1, new HoconNumber("3", true));
    var element2 = result.get(2);
    assertEquals(element2, new HoconString("five", false, false));
  }

  @Test
  public void testParseNestedList() throws HoconParseException {
    var result = HoconParser.of("[ 1, 3, [2, 4] ]").parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(element0, new HoconNumber("1", true));
    var element1 = result.get(1);
    assertEquals(element1, new HoconNumber("3", true));
    var element2 = result.getPath("2.0");
    assertEquals(element2, new HoconNumber("2", true));
    var element3 = result.getPath("2.1");
    assertEquals(element3, new HoconNumber("4", true));
  }

  // IndexOutOfBoundExceptions should be gathered at the root of parsing and
  // transform into HoconParseException instead of being transferred here
  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testParseIncompleteList() throws HoconParseException {
    var result = HoconParser.of("[ 1, 3, ").parseList(0, null).unwrap();
    fail();
  }

  // IndexOutOfBoundExceptions should be gathered at the root of parsing and
  // transform into HoconParseException instead of being transferred here
  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testParseIncompleteList2() throws HoconParseException {
    var result = HoconParser.of("[ ").parseList(0, null).unwrap();
    fail();
  }

  @Test(expected = HoconParseException.class)
  public void testParseNotAList() throws HoconParseException {
    var result = HoconParser.of("1, 3 ").parseList(0, null).unwrap();
    fail();
  }
}
