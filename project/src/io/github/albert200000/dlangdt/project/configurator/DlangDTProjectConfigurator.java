package io.github.albert200000.dlangdt.project.configurator;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

import io.github.albert200000.dlangdt.project.builder.DNature;

public class DlangDTProjectConfigurator implements ProjectConfigurator {

    private static final String DUB_JSON = "dub.json";
    private static final String DUB_SDL = "dub.sdl";

    @Override
    public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
        Set<File> locations = new HashSet<>();
        findDubDirectories(root, locations, monitor);
        return locations;
    }

    private void findDubDirectories(File dir, Set<File> locations, IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            return;
        }
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.isDirectory()) {
                findDubDirectories(entry, locations, monitor);
            } else if (DUB_JSON.equals(entry.getName()) || DUB_SDL.equals(entry.getName())) {
                locations.add(entry.getParentFile());
            }
        }
    }

    @Override
    public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
        try {
            return hasDubConfig(container, monitor);
        } catch (CoreException e) {
            // Log rather than printStackTrace in production code
            return false;
        }
    }

    private boolean hasDubConfig(IContainer container, IProgressMonitor monitor) throws CoreException {
        if (monitor != null && monitor.isCanceled()) {
            return false;
        }
        IResource[] members = container.members();
        for (IResource member : members) {
            String name = member.getName();
            if (DUB_JSON.equals(name) || DUB_SDL.equals(name)) {
                return true;
            }
            if (member.getType() == IResource.FOLDER) {
                if (hasDubConfig((IContainer) member, monitor)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
        try {
            if (project.hasNature(DNature.NATURE_ID)) {
                return false;
            }
            return hasDubConfig(project, monitor);
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public void configure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
        try {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            String[] newNatures = new String[natures.length + 1];
            System.arraycopy(natures, 0, newNatures, 0, natures.length);
            newNatures[natures.length] = DNature.NATURE_ID;
            description.setNatureIds(newNatures);
            project.setDescription(description, monitor);
        } catch (CoreException e) {
            // Log the failure; do not silently swallow
        }
    }

    @Override
    public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
        // Let Eclipse's default import handling decide which folders to ignore.
        return Collections.emptySet();
    }
}