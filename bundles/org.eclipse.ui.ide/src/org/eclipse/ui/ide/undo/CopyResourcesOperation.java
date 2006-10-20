/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * A CopyResourcesOperation represents an undoable operation for copying one or
 * more resources in the workspace. Clients may call the public API from a
 * background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public class CopyResourcesOperation extends
		AbstractCopyOrMoveResourcesOperation {

	IResource[] originalResources;
	
	ResourceDescription[] originalResourceDescriptions;

	IPath[] originalDestinationPaths = null;

	IPath originalDestination = null;

	/**
	 * Create a CopyResourcesOperation that copies a single resource to a new
	 * location. The new location includes the name of the copy.
	 * 
	 * @param resource
	 *            the resource to be copied
	 * @param newPath
	 *            the new workspace-relative path for the copy, including its
	 *            desired name.
	 * @param label
	 *            the label of the operation
	 */
	public CopyResourcesOperation(IResource resource, IPath newPath,
			String label) {
		super(new IResource[] { resource }, new IPath[] { newPath }, label);
		setOriginalResources(new IResource[] {resource});
		originalDestinationPaths = new IPath[] { newPath };
	}

	/**
	 * Create a CopyResourcesOperation that copies all of the specified
	 * resources to a single target location. The original resource name will be
	 * used when copied to the new location.
	 * 
	 * @param resources
	 *            the resources to be copied
	 * @param destinationPath
	 *            the workspace-relative destination path for the copied
	 *            resource.
	 * @param label
	 *            the label of the operation
	 */
	public CopyResourcesOperation(IResource[] resources, IPath destinationPath,
			String label) {
		super(resources, destinationPath, label);
		setOriginalResources(resources);
		originalDestination = destinationPath;
	}

	/**
	 * Create a CopyResourcesOperation that copies each of the specified
	 * resources to its corresponding destination path in the destination path
	 * array. The resource name for the target is included in the corresponding
	 * destination path.
	 * 
	 * @param resources
	 *            the resources to be copied
	 * @param destinationPaths
	 *            a workspace-relative destination path for each copied
	 *            resource, which includes the name of the resource at the new
	 *            destination
	 * @param label
	 *            the label of the operation
	 */
	public CopyResourcesOperation(IResource[] resources,
			IPath[] destinationPaths, String label) {
		super(resources, destinationPaths, label);
		setOriginalResources(resources);
		originalDestinationPaths = destinationPaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation copies the resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		moveOrCopy(monitor, uiInfo, false); // false = copy
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation deletes the previously made copies and restores any
	 * resources that were overwritten by the copy.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doUndo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		monitor.beginTask("", 2); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_CopyingResourcesProgress);
		// undoing a copy is first deleting the copied resources...
		WorkspaceUndoUtil.delete(resources, new SubProgressMonitor(monitor, 1),
				uiInfo, true);
		// then restoring any overwritten by the previous copy...
		WorkspaceUndoUtil.recreate(resourceDescriptions,
				new SubProgressMonitor(monitor, 1), uiInfo);
		setResourceDescriptions(new ResourceDescription[0]);
		// then setting the target resources and destination paths
		// back to the original ones
		setTargetResources(originalResources);
		if (originalDestination != null) {
			destination = originalDestination;
			destinationPaths = null;
		} else {
			destination = null;
			destinationPaths = originalDestinationPaths;
		}
		monitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#updateResourceChangeDescriptionFactory(org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory,
	 *      int)
	 */
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		boolean update = false;
		if (operation == UNDO) {
			for (int i = 0; i < resources.length; i++) {
				update = true;
				IResource resource = resources[i];
				factory.delete(resource);
			}
			for (int i = 0; i < resourceDescriptions.length; i++) {
				update = true;
				IResource resource = resourceDescriptions[i]
						.createResourceHandle();
				factory.create(resource);
			}
		} else {
			for (int i = 0; i < resources.length; i++) {
				update = true;
				IResource resource = resources[i];
				factory.copy(resource, getDestinationPath(resource, i));
			}
		}
		return update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation computes the ability to copy the resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeExecutionStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = super.computeExecutionStatus(monitor);
		if (status.isOK()) {
			status = computeMoveOrCopyStatus();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation computes the ability to delete the original copy and
	 * restore any overwritten resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeUndoableStatus(monitor);
		if (!status.isOK()) {
			return status;
		}
		// If the originals no longer exist, we do not want to attempt to
		// undo the copy which involves deleting the copies. They may be all we
		// have left.
		if (originalResources == null) {
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_InvalidRestoreInfo);
		}
		for (int i = 0; i < originalResources.length; i++) {
			if (!resources[i].exists()) {
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractResourcesOperation_InvalidRestoreInfo);
			}
		}
		// undoing a copy means deleting the copy that was made
		if (status.isOK()) {
			status = computeDeleteStatus();
		}
		// and if there were resources overwritten by the copy, can we still
		// recreate them?
		if (status.isOK() && resourceDescriptions != null
				&& resourceDescriptions.length > 0) {
			status = computeCreateStatus();
		}

		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation computes the ability to copy the resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeRedoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeRedoableStatus(monitor);
		if (status.isOK()) {
			status = computeMoveOrCopyStatus();
		}
		return status;
	}
	/*
	 * Record the original resources, including a resource description to describe it.
	 * This is so we can make sure the original resources and their subtrees are intact
	 * before allowing a copy to be undone.
	 */
	private void setOriginalResources(IResource[] originals) {
		originalResources = originals;
		originalResourceDescriptions = new ResourceDescription[originals.length];
		for (int i=0; i<originals.length; i++) {
			originalResourceDescriptions[i] = ResourceDescription.fromResource(originals[i]);
		}
	}
}
