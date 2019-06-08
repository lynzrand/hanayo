package cc.karenia.hanayo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cc.karenia.hanayo.types.HoconBoolean;
import cc.karenia.hanayo.types.HoconList;
import cc.karenia.hanayo.types.HoconMap;
import cc.karenia.hanayo.types.HoconNumber;
import cc.karenia.hanayo.types.HoconParseException;
import cc.karenia.hanayo.types.HoconString;
import cc.karenia.hanayo.types.HoconType;

public class ValueParserTest {
  @Test
  public void testParseNumber() throws HoconParseException {
    var result1 = HoconParser.of("1.0").parseValue(0, null).unwrap();
    assertEquals(new HoconNumber("1.0", false), result1);
    var result2 = HoconParser.of("1").parseValue(0, null).unwrap();
    assertEquals(new HoconNumber("1", true), result2);
  }

  @Test
  public void testParseString() throws HoconParseException {
    var result1 = HoconParser.of("string").parseValue(0, null).unwrap();
    assertEquals(new HoconString("string", false, false), result1);
    var result2 = HoconParser.of("\"string1\"").parseValue(0, null).unwrap();
    assertEquals(new HoconString("string1", true, false), result2);
    var result3 = HoconParser.of("\"\"\"string1\nstring2\"\"\"")
        .parseValue(0, null).unwrap();
    assertEquals(new HoconString("string1\nstring2", false, true), result3);
  }

  @Test
  public void testParseBoolean() throws HoconParseException {
    var result1 = HoconParser.of("true").parseValue(0, null).unwrap();
    assertEquals(new HoconBoolean(true), result1);
    var result2 = HoconParser.of("false").parseValue(0, null).unwrap();
    assertEquals(new HoconBoolean(false), result2);
  }

  @Test
  public void testParseList() throws HoconParseException {
    var result1 = HoconParser.of("[ 1, 2, nine ]").parseValue(0, null).unwrap();
    assertEquals(HoconType.List, result1.getType());
    var result1AsList = (HoconList) result1;
    assertEquals(new HoconNumber("1", true), result1AsList.get(0));
    assertEquals(new HoconNumber("2", true), result1AsList.get(1));
    assertEquals(new HoconString("nine", false, false), result1AsList.get(2));
  }

  @Test
  public void testParseMap() throws HoconParseException {
    var result1 = HoconParser.of("{ key: value, key2: value2 }")
        .parseValue(0, null).unwrap();
    assertEquals(HoconType.Map, result1.getType());
    var result1AsMap = (HoconMap) result1;
    assertEquals(new HoconString("value", false, false),
        result1AsMap.get("key"));
    assertEquals(new HoconString("value2", false, false),
        result1AsMap.get("key2"));
  }

  @Test
  public void testConcatString() throws HoconParseException {
    var result1 = HoconParser.of("123abc").parseValue(0, null).unwrap();
    assertEquals(new HoconString("123abc", false, false), result1);
  }

  @Test
  public void testConcatMap() throws HoconParseException {
    var result1 = HoconParser.of("{ key: value } { key: value2, key2: value3 }")
        .parseValue(0, null).unwrap();
    assertEquals(HoconType.Map, result1.getType());
    var result1AsMap = (HoconMap) result1;
    assertEquals(new HoconString("value2", false, false),
        result1AsMap.get("key"));
    assertEquals(new HoconString("value3", false, false),
        result1AsMap.get("key2"));
  }

  @Test
  public void testConcatNestedMap() throws HoconParseException {
    var result1 = HoconParser.of("{ key.next: value } { key.next: value2}")
        .parseValue(0, null).unwrap();
    assertEquals(HoconType.Map, result1.getType());
    var result1AsMap = (HoconMap) result1;
    assertEquals(new HoconString("value2", false, false),
        result1AsMap.getPath("key.next"));
  }

  @Test
  public void testConcatList() throws HoconParseException {
    var result1 = HoconParser.of("[ a, b, c ] [ d, e, f ]").parseValue(0, null)
        .unwrap();
    assertEquals(HoconType.List, result1.getType());
    var result1AsList = (HoconList) result1;
    assertEquals(6, result1AsList.size());

    assertEquals(new HoconString("a", false, false), result1AsList.get(0));
    assertEquals(new HoconString("f", false, false), result1AsList.get(5));
  }

  @Test
  public void testConcatUnrelated() throws HoconParseException {
    var result1 = HoconParser.of("textValue { key: value }").parseValue(0, null)
        .unwrap();
    assertEquals(HoconType.Map, result1.getType());
    var result1AsMap = (HoconMap) result1;
    assertEquals(new HoconString("value", false, false),
        result1AsMap.get("key"));

    var result2 = HoconParser.of("textValue [ value ]").parseValue(0, null)
        .unwrap();
    assertEquals(HoconType.List, result2.getType());
    var result2AsList = (HoconList) result2;
    assertEquals(new HoconString("value", false, false), result2AsList.get(0));

    var result3 = HoconParser.of("{ key: value } [ value ]").parseValue(0, null)
        .unwrap();
    assertEquals(HoconType.List, result3.getType());
    var result3AsList = (HoconList) result3;
    assertEquals(new HoconString("value", false, false), result3AsList.get(0));

    var result4 = HoconParser.of("[ value ] { key: value }").parseValue(0, null)
        .unwrap();
    assertEquals(HoconType.Map, result4.getType());
    var result4AsMap = (HoconMap) result4;
    assertEquals(new HoconString("value", false, false),
        result4AsMap.get("key"));
  }
}
