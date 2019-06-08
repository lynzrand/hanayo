package cc.karenia.hanayo;

import static org.junit.Assert.*;
import org.junit.*;

import org.junit.Test;

import cc.karenia.hanayo.types.HoconParseException;

public class NumberParserTest {

  @Test
  public void TestRegularNumber() throws HoconParseException {
	  String number1 = "1000 ";
	  String number2 = "+1200 ";
	  String number3 = "-2000 ";
	  String number4 = "521.1 ";
	  String number5 = "112E3 ";
	  String number6 = "121e2 ";
	  String number7 = "+521.1 ";
	  String number8 = "-123E4 ";
	  String number9 = "+121e3 ";

		HoconParser parseParse = HoconParser.of(number1);
	    var parseResult = parseParse.parseNumber(0);
	    assertEquals(parseResult.result.isInteger, true);
	    assertEquals(parseResult.result.value, "1000");
	    assertEquals(parseResult.result.asInt(), 1000);

	    HoconParser parseParse2 = HoconParser.of(number2);
	    parseResult = parseParse2.parseNumber(0);
	    assertEquals(parseResult.result.isInteger, true);
	    assertEquals(parseResult.result.value, "+1200");
	    assertEquals(parseResult.result.asInt(), 1200);

	    parseResult = HoconParser.parseNumber(0);
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
}
