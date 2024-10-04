package org.eclipse.ui.internal.texteditor.spelling.util;

/**
 * Definition of Java partitioning and its partitions.
 *
 * Originally since org.eclipse.jdt.ui version 3.1
 *
 * @since 1.21
 */
public interface IJavaPartitions {

	/**
	 * The identifier of the Java partitioning.
	 */
	String JAVA_PARTITIONING = "___java_partitioning"; //$NON-NLS-1$

	/**
	 * The identifier of the single-line (JLS2: EndOfLineComment) end comment
	 * partition content type.
	 */
	String JAVA_SINGLE_LINE_COMMENT = "__java_singleline_comment"; //$NON-NLS-1$

	/**
	 * The identifier multi-line (JLS2: TraditionalComment) comment partition
	 * content type.
	 */
	String JAVA_MULTI_LINE_COMMENT = "__java_multiline_comment"; //$NON-NLS-1$

	/**
	 * The identifier of the Javadoc (JLS2: DocumentationComment) partition content
	 * type.
	 */
	String JAVA_DOC = "__java_javadoc"; //$NON-NLS-1$

	/**
	 * The identifier of the Java string partition content type.
	 */
	String JAVA_STRING = "__java_string"; //$NON-NLS-1$

	/**
	 * The identifier of the Java character partition content type.
	 */
	String JAVA_CHARACTER = "__java_character"; //$NON-NLS-1$

	/**
	 * The identifier multi-line (JEP 355: Text Block) String partition content
	 * type.
	 *
	 * @since 3.20
	 */
	String JAVA_MULTI_LINE_STRING = "__java_multiline_string"; //$NON-NLS-1$
	/**
	 * @since 1.21
	 */
	String JAVA_MARKDOWN_COMMENT = "__java_markdown_comment"; //$NON-NLS-1$
}