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

		HoconParser parseParse3 = HoconParser.of(number3);
		parseResult = parseParse3.parseNumber(0);
		assertEquals(parseResult.result.isInteger, true);
		assertEquals(parseResult.result.value, "-2000");
		assertEquals(parseResult.result.asInt(), -2000);

		HoconParser parseParse4 = HoconParser.of(number4);
		parseResult = parseParse4.parseNumber(0);
		assertEquals(parseResult.result.isInteger, false);
		assertEquals(parseResult.result.value, "521.1");
		assertEquals(parseResult.result.asDouble(), 521.1, 0.1);

		HoconParser parseParse5 = HoconParser.of(number5);
		parseResult = parseParse5.parseNumber(0);
		assertEquals(parseResult.result.isInteger, false);
		assertEquals(parseResult.result.value, "112E3");
		assertEquals(parseResult.result.asDouble(), 112000, 1);

		HoconParser parseParse6 = HoconParser.of(number6);
		parseResult = parseParse6.parseNumber(0);
		assertEquals(parseResult.result.isInteger, false);
		assertEquals(parseResult.result.value, "121e2");
		assertEquals(parseResult.result.asDouble(), 12100, 1);

		HoconParser parseParse7 = HoconParser.of(number7);
		parseResult = parseParse7.parseNumber(0);
		assertEquals(parseResult.result.isInteger, false);
		assertEquals(parseResult.result.value, "+521.1");
		assertEquals(parseResult.result.asDouble(), 521.1, 0.1);

		HoconParser parseParse8 = HoconParser.of(number8);
		parseResult = parseParse8.parseNumber(0);
		assertEquals(parseResult.result.isInteger, false);
		assertEquals(parseResult.result.value, "-123E4");
		assertEquals(parseResult.result.asDouble(), -1230000, 1);

		HoconParser parseParse9 = HoconParser.of(number9);
		parseResult = parseParse9.parseNumber(0);
		assertEquals(parseResult.result.isInteger, false);
		assertEquals(parseResult.result.value, "+121e3");
		assertEquals(parseResult.result.asDouble(), 121000, 1);
	}
}
