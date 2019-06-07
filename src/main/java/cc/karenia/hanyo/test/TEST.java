package cc.karenia.hanyo.test;

import static org.junit.Assert.assertEquals;

import cc.karenia.hanayo.HoconParser;
import cc.karenia.hanayo.types.HoconParseException;

public class TEST {
	public static void main(String args[]) throws HoconParseException
	{
		 var charArray = "test.\"quoted\".spaced key".toCharArray();
		    var parseResult = HoconParser.parseKey(charArray, 0);
		    assertEquals(parseResult.parseSuccess, true);
		   System.out.println(parseResult.result.next.name);
	}
}
