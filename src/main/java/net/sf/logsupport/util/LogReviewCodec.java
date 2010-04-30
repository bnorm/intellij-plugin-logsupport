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

package net.sf.logsupport.util;

import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.ui.util.Dialogs;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes and reads log reviews.
 * <p/>
 * Note: Parts of the code are derived from IntentionPowerPack.
 *
 * @author Juergen_Kellerer, 2010-04-18
 * @version 1.0
 */
public class LogReviewCodec extends LogMessageUtil {

	public static final String MARKER_CONSTANT_VALUE = "#" + VARIABLE_ARTIFACT + "#";
	public static final String MARKER_CONSTANT_BREAK = "#" + DELIMITED_ARTIFACT + "#";
	public static final Pattern SPLIT_PATTERN = Pattern.compile(
			"(" + Pattern.quote(MARKER_CONSTANT_VALUE) + "|" + Pattern.quote(MARKER_CONSTANT_BREAK) + ")");

	File logReview;

	public LogReviewCodec(File logReview) {
		this.logReview = logReview;
	}

	private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				InputStream source = !systemId.startsWith("file:") ? null :
						getClass().getResourceAsStream("/net/sf/logsupport/" + new File(URI.create(systemId)).getName());
				return source == null ? new InputSource(new StringReader("")) : new InputSource(source);
			}
		});
		return builder;
	}

	@NotNull 
	public List<LogMessage> decode() throws IOException {

		List<LogMessage> results = new ArrayList<LogMessage>();

		Document source;
		try {
			DocumentBuilder builder = createDocumentBuilder();
			source = builder.parse(logReview);
		} catch (Exception e) {
			throw new IOException(e);
		}

		Element table = null;
		NodeList nl = source.getElementsByTagName("table");
		for (int i = 0, len = nl.getLength(); i < len; i++)
			if ("reviewTable".equals(((Element)nl.item(i)).getAttribute("id")))
				table = (Element) nl.item(i);

		if (table == null)
			throw new IOException("Table element not found inside document.");

		NodeList rows = table.getElementsByTagName("tr");
		if (rows == null || rows.getLength() == 0)
			throw new IOException("The table doesn't contain any useful rows.");

		for (int i = 1, len = rows.getLength(); i < len; i++) {
			Element row = (Element) rows.item(i);
			String logId = "", logMessage = "", id = row.getAttribute("id");
			NodeList columns = row.getElementsByTagName("td");

			if (columns == null || id == null || id.isEmpty())
				continue;

			for (int j = 0, jlen = columns.getLength(); j < jlen; j++) {
				Element td = (Element) columns.item(j);
				String className = td.getAttribute("class");
				if ("logId".equals(className))
					logId = td.getTextContent();
				else if ("logMessage".equals(className))
					logMessage = td.getTextContent();
			}

			if (logMessage != null) {
				if (logId != null)
					logMessage = logId + logMessage;
				
				int idx = 0;
				Matcher matcher = SPLIT_PATTERN.matcher(logMessage);
				List<String> messageArtifacts = new ArrayList<String>();

				if (matcher.find()) {
					do {
						messageArtifacts.add(logMessage.substring(idx, matcher.start()));
						if (matcher.group().equals(MARKER_CONSTANT_BREAK))
							messageArtifacts.add(DELIMITED_ARTIFACT);
						else if (matcher.group().equals(MARKER_CONSTANT_VALUE))
							messageArtifacts.add(VARIABLE_ARTIFACT);
						idx = matcher.end();
					} while (matcher.find());

					messageArtifacts.add(logMessage.substring(idx));
					
				} else
					messageArtifacts.add(logMessage);

				results.add(newMessage(id, null, logId,
						messageArtifacts.toArray(new String[messageArtifacts.size()])));
			}
		}

		return results;
	}

	public void encode(List<PsiMethodCallExpression> expressionList) throws IOException {
		if (expressionList == null || expressionList.isEmpty()) {
			Messages.showInfoMessage(
					"Did not find any log messages inside this project.",
					"No log messages found.");
			return;
		}

		Document template;
		try {
			DocumentBuilder builder = createDocumentBuilder();
			template = builder.parse(getClass().getResourceAsStream("/net/sf/logsupport/LogReview.template.xhtml"));
		} catch (Exception e) {
			throw new IOException(e);
		}

		String title = "Log Review for Project \"" + expressionList.get(0).getProject().getName() + '"';
		template.getElementsByTagName("title").item(0).setTextContent(title);
		template.getElementsByTagName("h1").item(0).setTextContent(title);

		Element constantWrapper = template.getElementById("constantWrapper");
		Element templateRow = template.getElementById("templateRow");
		Element templateInsertPosition = (Element) templateRow.getParentNode();
		templateInsertPosition.removeChild(templateRow);

		for (Map.Entry<LogMessage, List<LogMessage>> entry : toMessages(expressionList, false).entrySet()) {
			Element row = (Element) templateRow.cloneNode(true);

			NodeList nl = row.getElementsByTagName("td");
			for (int i = 0, len = nl.getLength(); i < len; i++) {
				Element td = (Element) nl.item(i);
				String className = td.getAttribute("class");
				if ("logId".equals(className))
					td.setTextContent(entry.getKey().logId);
				else if ("logLevel".equals(className))
					td.setTextContent(entry.getKey().logLevel);
				else if ("logSource".equals(className)) {
					td.setTextContent("");
					boolean first = true;
					for (LogMessage logEntry : entry.getValue()) {
						if (first)
							first = false;
						else
							td.appendChild(template.createElement("br"));
						td.appendChild(template.createTextNode(logEntry.source.concat("\n")));
					}
				} else if ("logMessage".equals(className)) {
					Element tr = (Element) td.getParentNode();
					tr.setAttribute("id", entry.getKey().id);

					td.setTextContent("");
					for (MessageArtifact artifact : entry.getKey().logMessage) {
						if (artifact.isEditable()) {
							String t = artifact.toString(), logId = entry.getKey().logId;
							if (logId != null && t.startsWith(logId))
								t = t.substring(logId.length());
							td.appendChild(template.createTextNode(t));
						} else {
							String text = artifact.toString();
							if (VARIABLE_ARTIFACT.equals(text))
								text = MARKER_CONSTANT_VALUE;
							else if (DELIMITED_ARTIFACT.equals(text))
								text = MARKER_CONSTANT_BREAK;

							Element constant = (Element) constantWrapper.cloneNode(false);
							constant.removeAttribute("id");
							constant.setTextContent(text);
							td.appendChild(constant);
						}
					}
				}
			}

			// Add the new content
			nl = row.getChildNodes();
			for (int i = 0, len = nl.getLength(); i < len; i++) {
				Node n = nl.item(i);
				templateInsertPosition.appendChild(template.importNode(n, true));
			}
		}

		// Write the document.
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, UTF8.name());
			t.transform(new DOMSource(template), new StreamResult(logReview));
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
}
