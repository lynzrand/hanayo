package cc.karenia.hanayo;

import org.junit.*;

import cc.karenia.hanayo.types.HoconList;
import cc.karenia.hanayo.types.HoconMap;
import cc.karenia.hanayo.types.HoconNumber;
import cc.karenia.hanayo.types.HoconParseException;
import cc.karenia.hanayo.types.HoconType;

import static org.junit.Assert.*;

/**
 * Tests for document parsers
 */
public class DocumentParserTest {
  static {
    HoconParseException.shouldGatherStacktrace = true;
  }

  @Test
  public void testParseSimpleDocument() throws HoconParseException {
    var result = HoconParser.of("{\n  key: value\n}").parseDocument();
    assertEquals(result.getType(), HoconType.Map);
    var mapResult = (HoconMap) result;
    var resultValue = mapResult.get("key");
    assertEquals(resultValue.asString(), "value");
  }

  @Test
  public void testParseNestedMapDocument() throws HoconParseException {
    var result = HoconParser.of("{\n  key: { else-key: value }, \n}")
        .parseDocument();
    assertEquals(result.getType(), HoconType.Map);
    var mapResult = (HoconMap) result;
    var outerResultValue = mapResult.get("key");
    assertEquals(outerResultValue.getType(), HoconType.Map);
    var innerResultValue = ((HoconMap) outerResultValue).get("else-key");
    assertEquals(innerResultValue.asString(), "value");
  }

  @Test
  public void testParseNestedKeyDocument() throws HoconParseException {
    var result = HoconParser.of("{\n  else.key: value , \n}").parseDocument();
    assertEquals(result.getType(), HoconType.Map);
    var mapResult = (HoconMap) result;
    var outerResultValue = mapResult.get("else");
    assertEquals(outerResultValue.getType(), HoconType.Map);
    var innerResultValue = ((HoconMap) outerResultValue).get("key");
    assertEquals(innerResultValue.asString(), "value");
    System.out.println(result.toString());
  }

  @Test
  public void testParseNestedArrayMapDocument() throws HoconParseException {
    var result = HoconParser.of("{\n  key [ 1, 2, 4, ], \n}").parseDocument();
    assertEquals(HoconType.Map, result.getType());
    var mapResult = (HoconMap) result;
    var outerResultValue = mapResult.get("key");
    System.out.println(result.toString());
    assertEquals(HoconType.List, outerResultValue.getType());
    var innerResultValue = ((HoconList) outerResultValue).get(1);
    assertEquals("2", innerResultValue.asString());
  }

  @Test
  public void testParseSubstitution() throws HoconParseException {
    var result = HoconParser.of("{\n  key [ 1, 2, ${key.1} ], \n}")
        .parseDocument();
    assertEquals(HoconType.Map, result.getType());
    var mapResult = (HoconMap) result;
    var outerResultValue = mapResult.get("key");
    System.out.println(result.toString());
    assertEquals(HoconType.List, outerResultValue.getType());
    var innerResultValue = ((HoconList) outerResultValue).get(2);
    assertEquals("2", innerResultValue.asString());
    assertEquals(HoconType.Number, innerResultValue.getType());
  }

  @Test
  public void testParseDeterminedSubstitution() throws HoconParseException {
    var result = HoconParser
        .of("{\n  name: rynco, greeting: hello my name is ${name} \n}")
        .parseDocument();
    assertEquals(HoconType.Map, result.getType());
    assertEquals("hello my name is rynco",
        result.getPath("greeting").asString());
  }

  @Test
  public void testParseUndeterminedSubstitution() throws HoconParseException {
    var result = HoconParser.of("{\n  greeting: hello my name is ${?name} \n}")
        .parseDocument();
    assertEquals(HoconType.Map, result.getType());
    assertEquals("hello my name is", result.getPath("greeting").asString());
  }

  @Test
  public void testParseListSubstitutionConcat() throws HoconParseException {
    var result = HoconParser.of("{\n  key: [1]\n  key: ${?key} [2] \n}")
        .parseDocument();
    assertEquals(new HoconNumber("2", true), result.getPath("key.1"));
  }
}
