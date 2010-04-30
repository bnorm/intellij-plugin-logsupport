/*
 * Copyright 2010, Juergen Kellerer and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.logsupport.inspections;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Smoke test on the multi-format log message formatting inspection.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
@Ignore("requires fixing in maven build") 
public class VerifyFormattedMessageTest {

	VerifyFormattedMessage inspection = new VerifyFormattedMessage();
	String customMessageTemplate = "This is a {} message with placeholders.{}";
	String mfTemplate = "{0} is a {2} message with {1,number}{1,number} placeholders.";

	@Test
	public void testCustomReportsTooMany() {
		String result = inspection.evaluateCustom(customMessageTemplate, "\\{\\}", "custom", 2, "third");
		assertEquals(inspection.createInvalidArgumentCountMessage(2, 3), result);
	}

	@Test
	public void testCustomReportsNotEnough() {
		String result = inspection.evaluateCustom(customMessageTemplate, "\\{\\}", "custom");
		assertEquals(inspection.createInvalidArgumentCountMessage(2, 1), result);
	}

	@Test
	public void testCustomPassesValid() {
		assertNull(inspection.evaluateCustom(customMessageTemplate, "\\{\\}", "custom", 2));
	}

	@Test
	public void testMessageFormatReportsTooMany() {
		String result = inspection.evaluateMessageFormat(mfTemplate, "first", 2, "custom", "fourth");
		assertEquals(inspection.createInvalidArgumentCountMessage(3, 4), result);
	}

	@Test
	public void testMessageFormatReportsNotEnough() {
		String result = inspection.evaluateMessageFormat(mfTemplate, "first", 2);
		assertEquals(inspection.createInvalidArgumentCountMessage(3, 2), result);
	}

	@Test
	public void testMessageFormatReportsInvalidValue() {
		String result = inspection.evaluateMessageFormat(mfTemplate, "x", "y", "z");
		assertEquals(inspection.createInvalidArgumentTypeMessage(2), result);
	}

	@Test
	public void testMessageReportsInvalidPatternFormat() {
		assertEquals(inspection.createInvalidFormatMessage("can't parse argument number").trim(),
				inspection.evaluateMessageFormat("{} {0}", "first").trim());
		assertEquals(inspection.createInvalidArgumentCountMessage(0, 1),
				inspection.evaluateMessageFormat("' {0}", "first"));
	}

	@Test
	public void testMessageFormatPassesValid() {
		assertNull(inspection.evaluateMessageFormat(mfTemplate, "first", 2, "custom"));
		assertNull(inspection.evaluateMessageFormat(""));
	}

	@Test
	public void testPrintfPassesValid() {
		assertNull(inspection.evaluatePrintf("Message %s %d", "string", 9));
		assertNull(inspection.evaluatePrintf("Message"));
	}

	@Test
	public void testPrintfReportsInValid() {
		assertNotNull(inspection.evaluatePrintf("Message %s %d", 9, "string"));
		assertNotNull(inspection.evaluatePrintf("Message %s %d", "string"));
		assertNotNull(inspection.evaluatePrintf("Message %s %d", 9));
	}
}
