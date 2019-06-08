package cc.karenia.hanayo;

import static org.junit.Assert.*;
import org.junit.*;

import org.junit.Test;

import cc.karenia.hanayo.types.HoconParseException;

public class NumberParserTest {

  @Test
  public void TestRegularNumber() throws HoconParseException {
	  var number1 = "1000 ".toCharArray();
	    var number2 = "+1200 ".toCharArray();
	    var number3 = "-2000 ".toCharArray();
	    var number4 = "521.1 ".toCharArray();
	    var number5 = "112E3 ".toCharArray();
	    var number6 = "121e2 ".toCharArray();
	    var number7 = "+521.1 ".toCharArray();
	    var number8 = "-123E4 ".toCharArray();
	    var number9 = "+121e3 ".toCharArray();

	    var parseResult = HoconParser.parseNumber(number1, 0);
	    assertEquals(parseResult.result.isInteger, true);
	    assertEquals(parseResult.result.value, "1000");
	    assertEquals(parseResult.result.asInt(), 1000);

	    parseResult = HoconParser.parseNumber(number2, 0);
	    assertEquals(parseResult.result.isInteger, true);
	    assertEquals(parseResult.result.value, "+1200");
	    assertEquals(parseResult.result.asInt(), 1200);

	    parseResult = HoconParser.parseNumber(number3, 0);
	    assertEquals(parseResult.result.isInteger, true);
	    assertEquals(parseResult.result.value, "-2000");
	    assertEquals(parseResult.result.asInt(), -2000);

	    parseResult = HoconParser.parseNumber(number4, 0);
	    assertEquals(parseResult.result.isInteger, false);
	    assertEquals(parseResult.result.value, "521.1");
	    assertEquals(parseResult.result.asDouble(), 521.1, 0.1);

	    parseResult = HoconParser.parseNumber(number5, 0);
	    assertEquals(parseResult.result.isInteger, false);
	    assertEquals(parseResult.result.value, "112E3");
	    assertEquals(parseResult.result.asDouble(), 112000, 1);
	    
	    parseResult = HoconParser.parseNumber(number6, 0);
	    assertEquals(parseResult.result.isInteger, false);
	    assertEquals(parseResult.result.value, "121e2");
	    assertEquals(parseResult.result.asDouble(), 12100, 1);
	    
	    parseResult = HoconParser.parseNumber(number7, 0);
	    assertEquals(parseResult.result.isInteger, false);
	    assertEquals(parseResult.result.value, "+521.1");
	    assertEquals(parseResult.result.asDouble(), 521.1, 0.1);
	    
	    parseResult = HoconParser.parseNumber(number8, 0);
	    assertEquals(parseResult.result.isInteger, false);
	    assertEquals(parseResult.result.value, "-123E4");
	    assertEquals(parseResult.result.asDouble(), -1230000, 1);
	    
	    parseResult = HoconParser.parseNumber(number9, 0);
	    assertEquals(parseResult.result.isInteger, false);
	    assertEquals(parseResult.result.value, "+121e3");
	    assertEquals(parseResult.result.asDouble(), 121000, 1);
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
