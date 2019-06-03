package cc.karenia.hanayo;

import org.junit.*;

import cc.karenia.hanayo.types.HoconList;
import cc.karenia.hanayo.types.HoconMap;
import cc.karenia.hanayo.types.HoconParseException;
import cc.karenia.hanayo.types.HoconType;

import static org.junit.Assert.*;

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
  }

  @Test
  public void testParseNestedArrayMapDocument() throws HoconParseException {
    var result = HoconParser.of("{\n  key: [ 1, 2, 4, ], \n}").parseDocument();
    assertEquals(HoconType.Map, result.getType());
    var mapResult = (HoconMap) result;
    var outerResultValue = mapResult.get("key");
    System.out.println(result.toString());
    assertEquals(HoconType.List, outerResultValue.getType());
    var innerResultValue = ((HoconList) outerResultValue).get(1);
    assertEquals("2", innerResultValue.asString());
  }
}
