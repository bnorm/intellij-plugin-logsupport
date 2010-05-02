package net.sf.logsupport.util;

import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Defines a log message codec.
 *
 * @author Juergen_Kellerer, 2010-05-02
 * @version 1.0
 */
public interface Codec {

	/**
	 * Default codec selector.
	 */
	Selector SELECTOR = new Selector() {

		private ServiceLoader<Codec> loader;

		private ServiceLoader<Codec> getLoader() {
			if (loader == null)
				loader = ServiceLoader.load(Codec.class, getClass().getClassLoader());
			return loader;
		}

		@NotNull
		public Iterable<Codec> codecs() {
			return getLoader();
		}

		@Nullable
		public Codec select(@NotNull File file) throws IOException {
			for (Codec codec : getLoader()) {
				if (codec.isSupported(file))
					return codec;
			}
			return null;
		}
	};


	/**
	 * Returns the name of the codec.
	 *
	 * @return the name of the codec.
	 */
	String getName();

	/**
	 * Returns the default file name that is proposed by the codec.
	 *
	 * @return the default file name that is proposed by the codec.
	 */
	String getDefaultFilename();

	/**
	 * Returns true if the codec supports the specified file.
	 *
	 * @param logReview the log review file.
	 * @return true if the codec supports the specified file.
	 * @throws IOException if codec could not access the file.
	 */
	boolean isSupported(@NotNull File logReview) throws IOException;

	/**
	 * Decodes the log messages inside the given review file.
	 *
	 * @param logReview the file containing the log review.
	 * @return the log messages inside the given review file.
	 * @throws IOException if codec could not access or decode the file.
	 */
	@NotNull
	List<LogMessageUtil.LogMessage> decode(@NotNull File logReview) throws IOException;

	/**
	 * Encodes the log messages into the specified review file.
	 *
	 * @param expressionList the list of log calls to write into the review file.
	 * @param logReview	  the file to create.
	 * @throws IOException if codec could not access or encode the file.
	 */
	void encode(@NotNull List<PsiMethodCallExpression> expressionList, @NotNull File logReview) throws IOException;

	/**
	 * Selects the installed codecs.
	 */
	public interface Selector {
		@NotNull
		Iterable<Codec> codecs();

		@Nullable
		Codec select(@NotNull File file) throws IOException;
	}
}
