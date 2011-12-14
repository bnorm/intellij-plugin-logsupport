package net.sf.logsupport.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Reads and writes an OpenDocument Spreadsheet document on top of the XHTML codec.
 *
 * @author Juergen_Kellerer, 2010-05-02
 * @version 1.0
 */
public class ODSCodec implements Codec {

	private final XHTMLCodec xhtmlCodec = new XHTMLCodec();

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return "Open Document Spreadsheet (*.ods)";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultFilename() {
		return "log-review.ods";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupported(@NotNull File logReview) throws IOException {
		String name = logReview.getName().toLowerCase();
		return name.endsWith(".ods");
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public List<LogMessageUtil.LogMessage> decode(@NotNull File logReview) throws IOException {
		File tempReview = File.createTempFile("logsupport-", ".xhtml");
		try {
			ZipInputStream odsIn = new ZipInputStream(
					new BufferedInputStream(new FileInputStream(logReview)));

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer odsImport = tf.newTransformer(new StreamSource(getClass().
					getResourceAsStream("/net/sf/logsupport/LogReview.import.ods.xsl")));

			ZipEntry entry;
			while ((entry = odsIn.getNextEntry()) != null) {
				if ("content.xml".equals(entry.getName())) {
					odsImport.transform(new StreamSource(odsIn), new StreamResult(tempReview));
					break;
				}
			}
			odsIn.close();

			return xhtmlCodec.decode(tempReview);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (!tempReview.delete())
				tempReview.deleteOnExit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void encode(@NotNull List<PsiMethodCallExpression> expressionList,
					   @NotNull File logReview) throws IOException {
		File tempReview = File.createTempFile("logsupport-", ".xhtml");
		try {
			xhtmlCodec.encode(expressionList, tempReview);
			//DOMSource source = new DOMSource(XmlUtil.parse(tempReview));

			ZipInputStream templateIn = new ZipInputStream(getClass().
					getResourceAsStream("/net/sf/logsupport/LogReview.template.ods"));

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer odsContent = tf.newTransformer(new StreamSource(getClass().
					getResourceAsStream("/net/sf/logsupport/LogReview.template.ods.content.xsl")));
			Transformer odsStyle = tf.newTransformer(new StreamSource(getClass().
					getResourceAsStream("/net/sf/logsupport/LogReview.template.ods.styles.xsl")));

			ZipOutputStream odsOut = new ZipOutputStream(
					new BufferedOutputStream(new FileOutputStream(logReview)));

			ZipEntry entry;
			while ((entry = templateIn.getNextEntry()) != null) {
				boolean isStored = entry.getMethod() == ZipEntry.STORED;
				ZipEntry clone = isStored ? new ZipEntry(entry) : new ZipEntry(entry.getName());
				if (!isStored) {
					clone.setExtra(entry.getExtra());
					clone.setComment(entry.getComment());
					clone.setMethod(entry.getMethod());
				}

				odsOut.putNextEntry(clone);
				try {
					if (entry.isDirectory())
						continue;

					if ("content.xml".equals(entry.getName()))
						odsContent.transform(new StreamSource(tempReview), new StreamResult(odsOut));
					else if ("styles.xml".equals(entry.getName()))
						odsStyle.transform(new StreamSource(tempReview), new StreamResult(odsOut));
					else
						FileUtil.copy(templateIn, odsOut);
				} finally {
					odsOut.closeEntry();
				}
			}

			odsOut.close();

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (!tempReview.delete())
				tempReview.deleteOnExit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getName();
	}
}
