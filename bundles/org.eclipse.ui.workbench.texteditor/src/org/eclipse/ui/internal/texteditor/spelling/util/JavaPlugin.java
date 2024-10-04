/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.spelling.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;


/**
 * Represents the java plug-in. It provides a series of convenience methods such as
 * access to the workbench, keeps track of elements shared by all editors and viewers
 * of the plug-in such as document providers and find-replace-dialogs.
 */
public class JavaPlugin extends AbstractUIPlugin implements DebugOptionsListener {

	/**
	 * The key to store customized templates.
	 * @since 3.0
	 */
	private static final String TEMPLATES_KEY= "org.eclipse.jdt.ui.text.custom_templates"; //$NON-NLS-1$
	/**
	 * The key to store customized code templates.
	 * @since 3.0
	 */
	private static final String CODE_TEMPLATES_KEY= "org.eclipse.jdt.ui.text.custom_code_templates"; //$NON-NLS-1$

	public static boolean DEBUG_AST_PROVIDER;

	public static boolean DEBUG_BREADCRUMB_ITEM_DROP_DOWN;

	public static boolean DEBUG_RESULT_COLLECTOR;

	private static JavaPlugin fgJavaPlugin;

	private static LinkedHashMap<String, Long> fgRepeatedMessages= new LinkedHashMap<>(20, 0.75f, true) {
		private static final long serialVersionUID= 1L;
		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<String, Long> eldest) {
			return size() >= 20;
		}
	};

	/**
	 * The template context type registry for the java editor.
	 * @since 3.0
	 */
	private volatile ContextTypeRegistry fContextTypeRegistry;
	/**
	 * The code template context type registry for the java editor.
	 * @since 3.0
	 */
	private volatile ContextTypeRegistry fCodeTemplateContextTypeRegistry;

	/**
	 * The template store for the java editor.
	 * @since 3.0
	 */
	private volatile TemplateStore fTemplateStore;
	/**
	 * The coded template store for the java editor.
	 * @since 3.0
	 */
	private volatile TemplateStore fCodeTemplateStore;


	private static final String CODE_ASSIST_MIGRATED= "code_assist_migrated"; //$NON-NLS-1$

	private static final String TYPEFILTER_MIGRATED= "typefilter_migrated_2"; //$NON-NLS-1$


	/**
	 * The combined preference store.
	 * @since 3.0
	 */
	private volatile IPreferenceStore fCombinedPreferenceStore;


	/**
	 * The shared Java properties file document provider.
	 * @since 3.1
	 */
	private volatile IDocumentProvider fPropertiesFileDocumentProvider;




	/**
	 * Theme listener.
	 * @since 3.3
	 */
	private IPropertyChangeListener fThemeListener;

	private BundleContext fBundleContext;

	private ServiceRegistration<DebugOptionsListener> fDebugRegistration;

	public static JavaPlugin getDefault() {

		if (fgJavaPlugin == null) {
			fgJavaPlugin = new JavaPlugin();
		}

		return fgJavaPlugin;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		 IWorkbenchWindow window= getActiveWorkbenchWindow();
		 if (window != null) {
		 	return window.getShell();
		 }
		 return null;
	}

	public static String getPluginId() {
		return JavaUI.ID_PLUGIN;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi= new MultiStatus(getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, JavaUIMessages.JavaPlugin_internal_error, e));
	}

	/**
	 * Log a message that is potentially repeated after a very short time.
	 * The first time this method is called with a given message, the
	 * message is written to the log along with the detail message and a stack trace.
	 * <p>
	 * Only intended for use in debug statements.
	 *
	 * @param message the (generic) message
	 * @param detail the detail message
	 */
	public static void logRepeatedMessage(String message, String detail) {
		long now= System.currentTimeMillis();
		boolean writeToLog= true;
		if (fgRepeatedMessages.containsKey(message)) {
			long last= fgRepeatedMessages.get(message);
			writeToLog= now - last > 5000;
		}
		fgRepeatedMessages.put(message, now);
		if (writeToLog)
			log(new Exception(message + detail).fillInStackTrace());
	}

	public static boolean isDebug() {
		return getDefault().isDebugging();
	}


	public JavaPlugin() {
		super();
		fgJavaPlugin = this;
	}


	private void createOrUpdateWorkingSet(String name, String oldname, String label, final String id) {
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet= workingSetManager.getWorkingSet(name);
		if (workingSet == null) {
			workingSet= workingSetManager.createWorkingSet(name, new IAdaptable[0]);
			workingSet.setLabel(label);
			workingSet.setId(id);
			workingSetManager.addWorkingSet(workingSet);
		} else {
			if(id.equals(workingSet.getId())) {
				if (!label.equals(workingSet.getLabel()))
					workingSet.setLabel(label);
			} else {
				logErrorMessage("found existing workingset with name=\"" + name + "\" but id=\"" + workingSet.getId() + "\""); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			}
		}
		IWorkingSet oldWorkingSet= workingSetManager.getWorkingSet(oldname);
		if (oldWorkingSet != null && id.equals(oldWorkingSet.getId())) {
			workingSetManager.removeWorkingSet(oldWorkingSet);
		}
	}


	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	@Override
	protected ImageRegistry createImageRegistry() {
		return JavaPluginImages.getImageRegistry();
	}


	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getActivePage();
	}


	/**
	 * Returns the Java Core plug-in preferences.
	 *
	 * @return the Java Core plug-in preferences
	 * @since 3.7
	 */
	public static org.eclipse.core.runtime.Preferences getJavaCorePluginPreferences() {
		return JavaCore.getPlugin().getPluginPreferences();
	}





	/**
	 * Registers the given Java template context.
	 *
	 * @param registry the template context type registry
	 * @param id the context type id
	 * @param parent the parent context type
	 * @since 3.4
	 */
	private static void registerJavaContext(ContributionContextTypeRegistry registry, String id, TemplateContextType parent) {
		TemplateContextType contextType= registry.getContextType(id);
		Iterator<TemplateVariableResolver> iter= parent.resolvers();
		while (iter.hasNext())
			contextType.addResolver(iter.next());
	}


	/**
	 * Returns a section in the Java plugin's dialog settings. If the section doesn't exist yet, it is created.
	 *
	 * @param name the name of the section
	 * @return the section of the given name
	 * @since 3.2
	 */
	public IDialogSettings getDialogSettingsSection(String name) {
		IDialogSettings dialogSettings= getDialogSettings();
		IDialogSettings section= dialogSettings.getSection(name);
		if (section == null) {
			section= dialogSettings.addNewSection(name);
		}
		return section;
	}



	/**
	 * Returns the content assist additional info focus affordance string.
	 *
	 * @return the affordance string which is <code>null</code> if the
	 *			preference is disabled
	 *
	 * @see EditorsUI#getTooltipAffordanceString()
	 * @since 3.4
	 */
	public static final String getAdditionalInfoAffordanceString() {
		if (!EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
			return null;

		return JavaUIMessages.JavaPlugin_additionalInfo_affordance;
	}

	/**
	 * Returns the bundles for a given bundle name and version range,
	 * regardless whether the bundle is resolved or not.
	 *
	 * @param bundleName the bundle name
	 * @param version the version of the bundle, or <code>null</code> for all bundles
	 * @return the bundles of the given name belonging to the given version range
	 * @since 3.10
	 */
	public Bundle[] getBundles(String bundleName, String version) {
		Bundle[] bundles= Platform.getBundles(bundleName, version);
		if (bundles != null)
			return bundles;

		// Accessing unresolved bundle
		ServiceReference<PackageAdmin> serviceRef= fBundleContext.getServiceReference(PackageAdmin.class);
		PackageAdmin admin= fBundleContext.getService(serviceRef);
		bundles= admin.getBundles(bundleName, version);
		if (bundles != null && bundles.length > 0)
			return bundles;
		return null;
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		DEBUG_AST_PROVIDER= options.getBooleanOption("org.eclipse.jdt.ui/debug/ASTProvider", false); //$NON-NLS-1$
		DEBUG_BREADCRUMB_ITEM_DROP_DOWN= options.getBooleanOption("org.eclipse.jdt.ui/debug/BreadcrumbItemDropDown", false); //$NON-NLS-1$
		DEBUG_RESULT_COLLECTOR= options.getBooleanOption("org.eclipse.jdt.ui/debug/ResultCollector", false); //$NON-NLS-1$
	}

	/**
	 * Add only 'initializeCodeAssistCategoryDisabled(String)' calls here for
	 * proposal category id of *NEW* features meant to be disabled by deafult.
	 *
	 * Eg. initializeCodeAssistCategoryDisabled("org.eclipse.jdt.ui.javaPostfixProposalCategory"); //$NON-NLS-1$
	 *
	 * The call must be added here in addition to setting default disablement in
	 * {@link PreferenceConstants} CODEASSIST_EXCLUDED_CATEGORIES and CODEASSIST_CATEGORY_ORDER.
	 * This will only work correctly for newly added proposal categories for a given release.
	 */
	private static void disableNewCodeAssistCategoryPreferences() {
		// Eg. initializeCodeAssistCategoryDisabled("org.eclipse.jdt.ui.javaPostfixProposalCategory"); //$NON-NLS-1$
	}

	/**
	 * Disable (by default) the given category id for both the default content
	 * assist list (CODEASSIST_EXCLUDED_CATEGORIES) and the cycling content
	 * assist list (CODEASSIST_CATEGORY_ORDER)
	 *
	 * @param id The category id for the proposal feature to disable by default
	 */
	@SuppressWarnings("unused")
	private static void initializeCodeAssistCategoryDisabled(String id) {
		// If preference migrated, nothing to do
		if (isCodeAssistMigrated(id)) {
			return;
		}

		String currPrefExcludedValue= PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES);
		Set<String> disabled= new HashSet<>();
		StringTokenizer tok= new StringTokenizer(currPrefExcludedValue, "\0");  //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			disabled.add(tok.nextToken());
		}

		// preference not migrated, and not in user preferences
		if (!disabled.isEmpty() && !disabled.contains(id)) {
			String newPrefExcludedValue= currPrefExcludedValue + id + "\0"; //$NON-NLS-1$
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES, newPrefExcludedValue);

			// retrieve the id=rank to add from CODEASSIST_CATEGORY_ORDER from the default preferences
			String defPrefOrderValue= PreferenceConstants.getPreferenceStore().getDefaultString(PreferenceConstants.CODEASSIST_CATEGORY_ORDER);
			tok= new StringTokenizer(defPrefOrderValue, "\0"); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				StringTokenizer inner= new StringTokenizer(tok.nextToken(), ":"); //$NON-NLS-1$
				String key= inner.nextToken();
				int rank= Integer.parseInt(inner.nextToken());
				if (id.equals(key)) {
					String currPrefOrderValue= PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.CODEASSIST_CATEGORY_ORDER);
					String newPreferenceOrderValue= currPrefOrderValue + id + ":" + rank + "\0"; //$NON-NLS-1$ //$NON-NLS-2$
					PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.CODEASSIST_CATEGORY_ORDER, newPreferenceOrderValue);
				}
			}
		}

		// set as migrated
		setCodeAssistMigrated(id);
	}

	private static boolean isCodeAssistMigrated(String id) {
		String key= CODE_ASSIST_MIGRATED + "_" + id; //$NON-NLS-1$
		boolean res= Platform.getPreferencesService().getBoolean(JavaPlugin.getPluginId(), key, false, null);
		return res;
	}

	private static void setCodeAssistMigrated(String id) {
		String key= CODE_ASSIST_MIGRATED + "_" + id; //$NON-NLS-1$
		IEclipsePreferences preferences= InstanceScope.INSTANCE.getNode(JavaPlugin.getPluginId());
		preferences.putBoolean(key, true);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			JavaPlugin.log(e);
		}
	}

	/**
	 * Add the new default type filters in old workspaces that already have non-default type
	 * filters. Only do this once, so that users have a way to opt-out if they don't want the new
	 * filters.
	 */
	private void setTypeFilterPreferences() {
		Set<String> enabledFiltersToAdd= new LinkedHashSet<>();
		enabledFiltersToAdd.add("com.sun.*"); //$NON-NLS-1$
		enabledFiltersToAdd.add("sun.*"); //$NON-NLS-1$
		enabledFiltersToAdd.add("jdk.*"); //$NON-NLS-1$
		enabledFiltersToAdd.add("io.micrometer.shaded.*"); //$NON-NLS-1$
		enabledFiltersToAdd.add("java.awt.List"); //$NON-NLS-1$

		Set<String> disabledFiltersToAdd= new LinkedHashSet<>();
		disabledFiltersToAdd.add("java.rmi.*"); //$NON-NLS-1$
		disabledFiltersToAdd.add("org.graalvm.*"); //$NON-NLS-1$
		disabledFiltersToAdd.add("java.awt.*"); //$NON-NLS-1$

		// default value - enabled
		Set<String> defaultEnabled= new LinkedHashSet<>();
		String defaultEnabledString= PreferenceConstants.getPreferenceStore().getDefaultString(PreferenceConstants.TYPEFILTER_ENABLED);
		defaultEnabled.addAll(Arrays.asList(defaultEnabledString.split(";"))); //$NON-NLS-1$
		defaultEnabled.addAll(enabledFiltersToAdd);
		String newDefaultEnabledString= defaultEnabled.stream().collect(Collectors.joining(";")); //$NON-NLS-1$
		PreferenceConstants.getPreferenceStore().setDefault(PreferenceConstants.TYPEFILTER_ENABLED, newDefaultEnabledString);

		// default value - disabled
		Set<String> defaultDisabled= new LinkedHashSet<>();
		String defaultDisabledString= PreferenceConstants.getPreferenceStore().getDefaultString(PreferenceConstants.TYPEFILTER_DISABLED);
		defaultDisabled.addAll(Arrays.asList(defaultDisabledString.split(";"))); //$NON-NLS-1$
		defaultDisabled.addAll(disabledFiltersToAdd);
		String newDefaultDisabledString= defaultDisabled.stream().collect(Collectors.joining(";")); //$NON-NLS-1$
		PreferenceConstants.getPreferenceStore().setDefault(PreferenceConstants.TYPEFILTER_DISABLED, newDefaultDisabledString);

		if (isTypeFilterMigrated()) {
			return;
		}

		// current values
		Set<String> currentEnabled= new LinkedHashSet<>();
		String currentEnabledString= PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.TYPEFILTER_ENABLED);
		currentEnabled.addAll(Arrays.asList(currentEnabledString.split(";"))); //$NON-NLS-1$
		currentEnabled.remove("org.graalvm.*"); //$NON-NLS-1$
		currentEnabled.remove("java.awt.*"); //$NON-NLS-1$

		Set<String> currentDisabled= new LinkedHashSet<>();
		String currentDisabledString= PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.TYPEFILTER_DISABLED);
		currentDisabled.addAll(Arrays.asList(currentDisabledString.split(";"))); //$NON-NLS-1$

		enabledFiltersToAdd.removeAll(currentEnabled);
		enabledFiltersToAdd.removeAll(currentDisabled);

		disabledFiltersToAdd.removeAll(currentEnabled);
		disabledFiltersToAdd.removeAll(currentDisabled);

		if (!enabledFiltersToAdd.isEmpty()) {
			currentEnabledString = currentEnabled.stream().collect(Collectors.joining(";")); //$NON-NLS-1$
			String newEnabledString= currentEnabledString + ";" + enabledFiltersToAdd.stream().collect(Collectors.joining(";")); //$NON-NLS-1$ //$NON-NLS-2$
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.TYPEFILTER_ENABLED, newEnabledString);
		}

		if (!disabledFiltersToAdd.isEmpty()) {
			currentDisabledString = currentDisabled.stream().collect(Collectors.joining(";")); //$NON-NLS-1$
			String newDisabledString= currentDisabledString + ";" + disabledFiltersToAdd.stream().collect(Collectors.joining(";")); //$NON-NLS-1$ //$NON-NLS-2$
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.TYPEFILTER_DISABLED, newDisabledString);
		}

		// set as migrated
		setTypeFilterMigrated();
	}

	private boolean isTypeFilterMigrated() {
		return Platform.getPreferencesService().getBoolean(JavaPlugin.getPluginId(), TYPEFILTER_MIGRATED, false, null);
	}

	private void setTypeFilterMigrated() {
		IEclipsePreferences preferences= InstanceScope.INSTANCE.getNode(JavaPlugin.getPluginId());
		preferences.putBoolean(TYPEFILTER_MIGRATED, true);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			JavaPlugin.log(e);
		}
	}
}
