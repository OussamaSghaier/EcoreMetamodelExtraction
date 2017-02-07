package eme.generator.saving;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Saving strategy that saves the ecore file in the same project where it was extracted from.
 * @author Timur Saglam
 */
public class SameProjectSavingStrategy extends AbstractSavingStrategy {
    private String projectName;

    /**
     * Basic constructor.
     */
    public SameProjectSavingStrategy() {
        super(true); // refresh folder.
    }

    /*
     * @see eme.generator.saving.AbstractSavingStrategy#beforeSaving()
     */
    @Override
    protected void beforeSaving(String projectName) {
        this.projectName = projectName;
    }

    /*
     * @see eme.generator.saving.AbstractSavingStrategy#fileName()
     */
    @Override
    protected String getFileName() {
        return projectName;
    }

    /*
     * @see eme.generator.saving.AbstractSavingStrategy#filePath()
     */
    @Override
    protected String getFilePath() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace(); // TODO (MEDIUM) replace slashes with File.separator
        return workspace.getRoot().getLocation().toFile().getPath() + "/" + projectName + "/model/";
    }
}