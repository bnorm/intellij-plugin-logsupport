package net.sf.logsupport.util;

import org.w3c.dom.Document;
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
import java.nio.charset.Charset;

/**
 * Helper class for shared, XML related operations.
 *
 * @author Juergen_Kellerer, 2010-05-02
 * @version 1.0
 */
public class XmlUtil {

	static final Charset UTF8 = Charset.forName("UTF-8");

	public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				InputStream source = !systemId.startsWith("file:") ? null :
						getClass().getResourceAsStream("/net/sf/logsupport/" +
								new File(URI.create(systemId)).getName());

				return source == null ? new InputSource(new StringReader("")) : new InputSource(source);
			}
		});
		return builder;
	}

	public static Document parse(File file) throws IOException {
		try {
			return createDocumentBuilder().parse(file);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static Document parse(InputStream input) throws IOException {
		try {
			return createDocumentBuilder().parse(input);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static void serialize(Document document, File targetFile)  throws IOException {
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.transform(new DOMSource(document), new StreamResult(targetFile));
		} catch (TransformerException e) {
			throw new IOException(e);
		}		
	}

	private XmlUtil() {
	}
}
