
package com.adarsh.fbjavabot.facebook.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{

	private static String emailPatternStr = "(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	private static Pattern emailPattern;

	private static String creditCardPatterStr = "((?:(?:4\\d{3})|(?:5[1-5]\\d{2})|6(?:011|5[0-9]{2}))(?:-?|\\040?)(?:\\d{4}(?:-?|\\040?)){3}|(?:3[4,7]\\d{2})(?:-?|\\040?)\\d{6}(?:-?|\\040?)\\d{5})";

	private static final String DISALLOWED_SPECIAL_CHARS = "*,\\-, ,.,(,),+";

	static
	{
		emailPattern = Pattern.compile(StringUtils.emailPatternStr);
	}

	public static String maskCreditCard(String text)
	{
		if(text != null)
		{	
			if(text.chars().anyMatch(n -> Character.isDigit(n)))
			{
				text = text.replaceAll(creditCardPatterStr, "<cc# quarantined>");
			}
		}
		return text;
	}

	public static String getEmailFromText(String text)
	{
		if(text != null)
		{
			Matcher m = emailPattern.matcher(text);
			if(m.find())
			{
				return m.group();
			}
		}

		return null;
	}

	public static String getDigitsFromText(String text)
	{
		if(text != null && !text.isEmpty())
		{
			int[] capturedDigits = text.chars().filter(n -> Character.isDigit(n)).toArray();
			return capturedDigits.length > 0 ? new String(capturedDigits, 0, capturedDigits.length) : null;
		}

		return null;
	}

	public static String handleNull(String value)
	{
		return value == null ? "" : value;
	}

	public static String cleanse(String text)
	{
		if(text != null)
		{
			return text.replaceAll("[" + DISALLOWED_SPECIAL_CHARS + "]", "");
		}

		return text;
	}

	/**
	 * This method cleans up the booking description for display. 
	 * Sometimes SBS() returns booking descriptions with trailing dates. The bot explicitly 
	 * displays the date in a sub-title field. So we need to remove this from the booking title
	 * @param bookingDescription
	 * @return cleansedBookingDescription
	 */
	public static String cleanseBookingTitle(String bookingDescription)
	{
		if(bookingDescription == null) 
			return null;
		
		bookingDescription = bookingDescription.trim();
		
		// Date pattern (covering for both domestic and international format also looking for slashes and dashes between numbers
		String date = "\\d{1,4}[/-]\\d{1,2}[/-]\\d{2,4}";
		// trailing '?' means exactly 1 or 0 parentheses
		String openParen = "\\(?";
		String closeParen = "\\)?";
		// this covers everything from no space, no spaces but a dash, any number of spaces with or without dash
		String dash = "\\s*-*\\s*";
		// This will ensure we only take from the end (not dates in the middle
		String nothingFollows = "(?!.)";

		// put it all together
		List<String> patterns = new ArrayList<String>();
		// first remove dates at end
		patterns.add(openParen + date + dash + date + closeParen + nothingFollows);
		patterns.add(openParen + date + closeParen + nothingFollows);
		// remove any date
		patterns.add(date);
		patterns.add("\\(\\d+\\)"); // any numbers in parentheses
		
		for(String pattern : patterns)
		{
			bookingDescription = bookingDescription.replaceAll(pattern, "");
		}
		
		return bookingDescription.trim();
	}
}
