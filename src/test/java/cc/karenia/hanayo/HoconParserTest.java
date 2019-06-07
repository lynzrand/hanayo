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
		// key separator . \"
		var str = "test.\"quoted\".spaced key";
		var parseResult = HoconParser.of(str).parseKey(0);
		assertEquals(parseResult.parseSuccess, true);
		assertEquals(parseResult.result.name, "test");
		assertEquals(parseResult.result.next.name, "quoted");
		assertEquals(parseResult.result.next.next.name, "spaced key");
		// key separator {}//
		String str1 = "{test.\"quoted\".spaced key}";
		var parseResult1 = HoconParser.of(str).parseKey(0);
		assertEquals(parseResult1.parseSuccess, true);
		assertEquals(parseResult1.result.name, "test");
		assertEquals(parseResult1.result.next.name, "quoted");
		assertEquals(parseResult1.result.next.next.name, "spaced key");
		// key separator []//
		String str2 = "[[test.\"quoted\".spaced key]";
		var parseResult2 = HoconParser.of(str).parseKey(0);
		assertEquals(parseResult2.parseSuccess, true);
		assertEquals(parseResult2.result.name, "test");
		assertEquals(parseResult2.result.next.name, "quoted");
		assertEquals(parseResult2.result.next.next.name, "spaced key");

		// key separator value with ://
		String str3 = "test.\"quoted\".spaced key:value";
		var parseResult3 = HoconParser.of(str).parseKey(0);
		assertEquals(parseResult3.parseSuccess, true);
		assertEquals(parseResult3.result.name, "test");
		assertEquals(parseResult3.result.next.name, "quoted");
		assertEquals(parseResult3.result.next.next.name, "spaced key");
		//assertEquals(parseResult3, "value");

		// key separator value with =//
		String str4 = "test.\"quoted\".spaced key=value";
		var parseResult4 = HoconParser.of(str).parseKey(0);
		var parseResultV4 = HoconParser.of(str).parseValue(0, null);
		assertEquals(parseResult4.parseSuccess, true);
		assertEquals(parseResult4.result.name, "test");
		assertEquals(parseResult4.result.next.name, "quoted");
		assertEquals(parseResult4.result.next.next.name, "spaced key");
		assertEquals(parseResultV4.result.asString(),"value");

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
		var parseResult = HoconParser.of(valueTest).parseValueSegment(0, null).unwrapThrow();
		var valueParseResult = HoconParser.of(valueTest).parseValue(0, null).unwrapThrow();

		assertEquals(parseResult.asString(), "-123");
		assertEquals(valueParseResult.asString(), "-123abc");

	}
}
