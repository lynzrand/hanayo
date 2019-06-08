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
    assertEquals(new HoconNumber("1", true), element0);
    var element1 = result.get(1);
    assertEquals(new HoconNumber("2", true), element1);
    var element2 = result.get(2);
    assertEquals(new HoconString("three"), element2);
  }

  @Test
  public void testParseEolSeparatedList() throws HoconParseException {
    var result = HoconParser.of("[\r\n  1\r\n  3\r\n  five\r\n]")
        .parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(new HoconNumber("1", true), element0);
    var element1 = result.get(1);
    assertEquals(new HoconNumber("3", true), element1);
    var element2 = result.get(2);
    assertEquals(new HoconString("five", false, false), element2);
  }

  @Test
  public void testParseMixedSeparatorList() throws HoconParseException {
    var result = HoconParser.of("[\r\n  1, 2,\r\n  3, 4,\r\n  five, six\r\n]")
        .parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(new HoconNumber("1", true), element0);
    var element1 = result.get(1);
    assertEquals(new HoconNumber("2", true), element1);
    var element4 = result.get(4);
    assertEquals(new HoconString("five", false, false), element4);
    var element5 = result.get(5);
    assertEquals(new HoconString("six", false, false), element5);
  }

  @Test
  public void testParseCommentEnabledList() throws HoconParseException {
    var result = HoconParser.of(
        "[\r\n  1 // this is the first element\r\n  3 # this is the second element\r\n  five\r\n]")
        .parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(new HoconNumber("1", true), element0);
    var element1 = result.get(1);
    assertEquals(new HoconNumber("3", true), element1);
    var element2 = result.get(2);
    assertEquals(new HoconString("five", false, false), element2);
  }

  @Test
  public void testParseNestedList() throws HoconParseException {
    var result = HoconParser.of("[ 1, 3, [2, 4] ]").parseList(0, null).unwrap();
    assertEquals(HoconType.List, result.getType());
    var element0 = result.get(0);
    assertEquals(new HoconNumber("1", true), element0);
    var element1 = result.get(1);
    assertEquals(new HoconNumber("3", true), element1);
    var element2 = result.getPath("2.0");
    assertEquals(new HoconNumber("2", true), element2);
    var element3 = result.getPath("2.1");
    assertEquals(new HoconNumber("4", true), element3);
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
