package org.eclipse.ui.internal.texteditor.spelling.util;

/**
 * A callback interface for receiving java problem as they are discovered by
 * some Java operation.
 *
 * @see IProblem
 * @since 2.0
 */
public interface IProblemRequestor {

	/**
	 * Notification of a Java problem.
	 *
	 * @param problem IProblem - The discovered Java problem.
	 */
	void acceptProblem(IProblem problem);

	/**
	 * Notification sent before starting the problem detection process. Typically,
	 * this would tell a problem collector to clear previously recorded problems.
	 */
	void beginReporting();

	/**
	 * Notification sent after having completed problem detection process.
	 * Typically, this would tell a problem collector that no more problems should
	 * be expected in this iteration.
	 */
	void endReporting();

	/**
	 * Predicate allowing the problem requestor to signal whether or not it is
	 * currently interested by problem reports. When answering <code>false</code>,
	 * problem will not be discovered any more until the next iteration.
	 *
	 * This predicate will be invoked once prior to each problem detection
	 * iteration.
	 *
	 * @return boolean - indicates whether the requestor is currently interested by
	 *         problems.
	 */
	boolean isActive();
}