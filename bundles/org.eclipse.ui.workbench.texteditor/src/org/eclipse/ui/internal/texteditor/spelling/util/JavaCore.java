package org.eclipse.ui.internal.texteditor.spelling.util;

import org.eclipse.core.runtime.Plugin;

public class JavaCore extends Plugin {

	/**
	 * The plug-in identifier of the Java core support (value
	 * <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.jdt.core"; //$NON-NLS-1$

	/**
	 * Code assist option ID: Define the Prefixes for Field Name.
	 * <p>
	 * When the prefixes is non empty, completion for field name will begin with one
	 * of the proposed prefixes.
	 * </p>
	 * <dl>
	 * <dt>Option id:</dt>
	 * <dd><code>"org.eclipse.jdt.core.codeComplete.fieldPrefixes"</code></dd>
	 * <dt>Possible values:</dt>
	 * <dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
	 * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
	 * <dt>Default:</dt>
	 * <dd><code>""</code></dd>
	 * </dl>
	 *
	 * @since 2.1
	 * @category CodeAssistOptionID
	 */
	public static final String CODEASSIST_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.fieldPrefixes"; //$NON-NLS-1$
	/**
	 * Code assist option ID: Define the Prefixes for Static Field Name.
	 * <p>
	 * When the prefixes is non empty, completion for static field name will begin
	 * with one of the proposed prefixes.
	 * </p>
	 * <dl>
	 * <dt>Option id:</dt>
	 * <dd><code>"org.eclipse.jdt.core.codeComplete.staticFieldPrefixes"</code></dd>
	 * <dt>Possible values:</dt>
	 * <dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
	 * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
	 * <dt>Default:</dt>
	 * <dd><code>""</code></dd>
	 * </dl>
	 *
	 * @since 2.1
	 * @category CodeAssistOptionID
	 */
	public static final String CODEASSIST_STATIC_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.staticFieldPrefixes"; //$NON-NLS-1$

	/**
	 * Code assist option ID: Define the Suffixes for Field Name.
	 * <p>
	 * When the suffixes is non empty, completion for field name will end with one
	 * of the proposed suffixes.
	 * </p>
	 * <dl>
	 * <dt>Option id:</dt>
	 * <dd><code>"org.eclipse.jdt.core.codeComplete.fieldSuffixes"</code></dd>
	 * <dt>Possible values:</dt>
	 * <dd><code>{ "&lt;suffix&gt;[,&lt;suffix&gt;]*" }</code> where
	 * <code>&lt;suffix&gt;</code> is a String without any wild-card</dd>
	 * <dt>Default:</dt>
	 * <dd><code>""</code></dd>
	 * </dl>
	 *
	 * @since 2.1
	 * @category CodeAssistOptionID
	 */
	public static final String CODEASSIST_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.fieldSuffixes"; //$NON-NLS-1$

	/**
	 * Value of the content-type for Java source files. Use this value to retrieve
	 * the Java content type from the content type manager, and to add new Java-like
	 * extensions to this content type.
	 *
	 * @see org.eclipse.core.runtime.content.IContentTypeManager#getContentType(String)
	 * @see #getJavaLikeExtensions()
	 * @since 3.2
	 */
	public static final String JAVA_SOURCE_CONTENT_TYPE = JavaCore.PLUGIN_ID + ".javaSource"; //$NON-NLS-1$

	/**
	 * Compiler option ID: Defining the Automatic Task Tags.
	 * <p>
	 * When the tag list is not empty, the compiler will issue a task marker
	 * whenever it encounters one of the corresponding tags inside any comment in
	 * Java source code.
	 * </p>
	 * <p>
	 * Generated task messages will start with the tag, and range until the next
	 * line separator, comment ending, or tag.
	 * </p>
	 * <p>
	 * When a given line of code bears multiple tags, each tag will be reported
	 * separately. Moreover, a tag immediately followed by another tag will be
	 * reported using the contents of the next non-empty tag of the line, if any.
	 * </p>
	 * <p>
	 * Note that tasks messages are trimmed. If a tag is starting with a letter or
	 * digit, then it cannot be leaded by another letter or digit to be recognized
	 * (<code>"fooToDo"</code> will not be recognized as a task for tag
	 * <code>"ToDo"</code>, but <code>"foo#ToDo"</code> will be detected for either
	 * tag <code>"ToDo"</code> or <code>"#ToDo"</code>). Respectively, a tag ending
	 * with a letter or digit cannot be followed by a letter or digit to be
	 * recognized (<code>"ToDofoo"</code> will not be recognized as a task for tag
	 * <code>"ToDo"</code>, but <code>"ToDo:foo"</code> will be detected either for
	 * tag <code>"ToDo"</code> or <code>"ToDo:"</code>).
	 * </p>
	 * <p>
	 * Task Priorities and task tags must have the same length. If task tags are
	 * set, then task priorities should also be set.
	 * </p>
	 * <dl>
	 * <dt>Option id:</dt>
	 * <dd><code>"org.eclipse.jdt.core.compiler.taskTags"</code></dd>
	 * <dt>Possible values:</dt>
	 * <dd><code>{ "&lt;tag&gt;[,&lt;tag&gt;]*" }</code> where
	 * <code>&lt;tag&gt;</code> is a String without any wild-card or
	 * leading/trailing spaces</dd>
	 * <dt>Default:</dt>
	 * <dd><code>"TODO,FIXME,XXX"</code></dd>
	 * </dl>
	 *
	 * @since 2.1
	 * @category CompilerOptionID
	 * @see #COMPILER_TASK_PRIORITIES
	 */
	public static final String COMPILER_TASK_TAGS = PLUGIN_ID + ".compiler.taskTags"; //$NON-NLS-1$

	public static String getOption(String compilerTaskTags) {
		return null;
	}

	private static JavaCore JAVA_CORE_PLUGIN;

	public JavaCore() {
		super();
		JAVA_CORE_PLUGIN = this;
	}

	public static Plugin getPlugin() {

		if (JAVA_CORE_PLUGIN == null)
			JAVA_CORE_PLUGIN = new JavaCore();

		return JAVA_CORE_PLUGIN;
	}

}
