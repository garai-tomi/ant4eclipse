/**********************************************************************
 * Copyright (c) 2005-2007 ant4eclipse project team.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nils Hartmann, Daniel Kasmeroglu, Gerd Wuetherich
 **********************************************************************/
package org.ant4eclipse.pde.ant;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.ant4eclipse.jdt.ant.EcjAdditionalCompilerArguments;
import org.ant4eclipse.jdt.ant.ExecuteJdtProjectTask;
import org.ant4eclipse.jdt.tools.container.JdtClasspathContainerArgument;
import org.ant4eclipse.pde.model.buildproperties.PluginBuildProperties;
import org.ant4eclipse.pde.model.buildproperties.PluginBuildProperties.Library;
import org.ant4eclipse.pde.model.pluginproject.PluginProjectRole;
import org.ant4eclipse.pde.tools.PdeBuildHelper;
import org.ant4eclipse.platform.ant.core.MacroExecutionValues;
import org.ant4eclipse.platform.ant.core.ScopedMacroDefinition;
import org.ant4eclipse.platform.ant.core.delegate.MacroExecutionValuesProvider;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.types.FileList;
import org.osgi.framework.Version;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ExecutePluginProjectTask extends ExecuteJdtProjectTask implements TargetPlatformAwareComponent,
    PdeExecutorValues {
 
  /** - */
  private static final String         SCOPE_NAME_LIBRARY               = "ForEachPluginLibrary";

  /** - */
  public static final String          SCOPE_LIBRARY                    = "SCOPE_LIBRARY";

  /** - */
  private TargetPlatformAwareDelegate _targetPlatformAwareDelegate;

  /**
   * <p>
   * Creates a new instance of type {@link ExecutePluginProjectTask}.
   * </p>
   * 
   */
  public ExecutePluginProjectTask() {
    super("executePluginProject");

    _targetPlatformAwareDelegate = new TargetPlatformAwareDelegate();
  }

  /**
   * {@inheritDoc}
   */
  public final String getTargetPlatformId() {
    return _targetPlatformAwareDelegate.getTargetPlatformId();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isTargetPlatformIdSet() {
    return _targetPlatformAwareDelegate.isTargetPlatformIdSet();
  }

  /**
   * {@inheritDoc}
   */
  public final void setTargetPlatformId(String targetPlatformId) {
    _targetPlatformAwareDelegate.setTargetPlatformId(targetPlatformId);
  }

  /**
   * {@inheritDoc}
   */
  public final void requireTargetPlatformIdSet() {
    _targetPlatformAwareDelegate.requireTargetPlatformIdSet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addAdditionalExecutionValues(MacroExecutionValues executionValues) {
    final PluginProjectRole pluginProjectRole = PluginProjectRole.Helper.getPluginProjectRole(getEclipseProject());

    // "calculate" effective version, that is the version with replaced qualifier
    final Version effectiveVersion = PdeBuildHelper.resolveVersion(pluginProjectRole.getBundleDescription()
        .getVersion(), pluginProjectRole.getBuildProperties().getQualifier());

    // TODO
    executionValues.getProperties().put(BUNDLE_RESOLVED_VERSION, effectiveVersion.toString());
    executionValues.getProperties().put(BUNDLE_VERSION,
        pluginProjectRole.getBundleDescription().getVersion().toString());

    PluginBuildProperties buildProperties = pluginProjectRole.getBuildProperties();
    executionValues.getProperties().put(BUILD_PROPERTIES_BINARY_INCLUDES, buildProperties.getBinaryIncludesAsString());
    executionValues.getProperties().put(BUILD_PROPERTIES_BINARY_EXCLUDES, buildProperties.getBinaryExcludesAsString());
  }

  /**
   * {@inheritDoc}
   */
  protected Object onCreateDynamicElement(final String name) {

    if (SCOPE_NAME_LIBRARY.equalsIgnoreCase(name)) {
      return createScopedMacroDefinition(SCOPE_LIBRARY);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute() {

    // TODO: CHECK!
    JdtClasspathContainerArgument containerArgument = createJdtClasspathContainerArgument();
    containerArgument.setKey("target.platform");
    containerArgument.setValue(getTargetPlatformId());

    super.doExecute();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean onExecuteScopeMacroDefintion(ScopedMacroDefinition<String> scopedMacroDefinition) {

    // 1. Check required fields
    requireWorkspaceAndProjectNameSet();
    ensureRole(PluginProjectRole.class);

    // execute scoped macro definitions
    if (SCOPE_LIBRARY.equals(scopedMacroDefinition.getScope())) {
      executeLibraryScopedMacroDef(scopedMacroDefinition.getMacroDef());
      return true;
    } else {
      return false;
    }

  }

  /**
   * <p>
   * </p>
   * 
   * @param macroDef
   */
  private void executeLibraryScopedMacroDef(MacroDef macroDef) {

    // 2. Get libraries
    final PluginProjectRole pluginProjectRole = PluginProjectRole.Helper.getPluginProjectRole(getEclipseProject());
    final PluginBuildProperties pluginBuildProperties = pluginProjectRole.getBuildProperties();
    final Library[] libraries = pluginBuildProperties.getOrderedLibraries();
    final List<String> binaryIncludes = Arrays.asList(pluginBuildProperties.getBinaryIncludes());

    for (final Library library : libraries) {

      if (binaryIncludes.contains(library.getName())) {

        executeMacroInstance(macroDef, new MacroExecutionValuesProvider() {

          public MacroExecutionValues provideMacroExecutionValues(MacroExecutionValues values) {

            values.getProperties().put(LIBRARY_NAME, library.getName());

            if (library.isSelf()) {
              values.getProperties().put(LIBRARY_IS_SELF, "true");
              computeBinaryIncludeFilelist();
            } else {
              values.getProperties().put(LIBRARY_IS_SELF, "false");
            }

            EcjAdditionalCompilerArguments compilerArguments = getExecutorValuesProvider().provideExecutorValues(
                getJavaProjectRole(), getJdtClasspathContainerArguments(), values);

            File[] sourceFiles = getEclipseProject().getChildren(library.getSource());
            File[] outputFiles = getEclipseProject().getChildren(library.getOutput());

            values.getProperties().put(SOURCE_DIRECTORIES, convertToString(sourceFiles));
            values.getProperties().put(OUTPUT_DIRECTORIES, convertToString(outputFiles));

            values.getReferences().put(SOURCE_DIRECTORIES_PATH, convertToPath(sourceFiles));
            values.getReferences().put(OUTPUT_DIRECTORIES_PATH, convertToPath(outputFiles));

            for (final String sourceFolderName : library.getSource()) {
              final String outputFolderName = getJavaProjectRole().getOutputFolderForSourceFolder(sourceFolderName);
              final File sourceFolder = getEclipseProject().getChild(sourceFolderName);
              final File outputFolder = getEclipseProject().getChild(outputFolderName);
              compilerArguments.addSourceFolder(sourceFolder, outputFolder);
            }

            addAdditionalExecutionValues(values);

            return values;
          }
        });
      }
    }
  }

  /**
   * <p>
   * </p>
   * 
   */
  private void computeBinaryIncludeFilelist() {
    // TODO
    final PluginProjectRole pluginProjectRole = PluginProjectRole.Helper.getPluginProjectRole(getEclipseProject());
    final PluginBuildProperties pluginBuildProperties = pluginProjectRole.getBuildProperties();
    final Library[] libraries = pluginBuildProperties.getOrderedLibraries();
    final List<String> binaryIncludes = Arrays.asList(pluginBuildProperties.getBinaryIncludes());

    FileList fileList = new FileList();
    fileList.setDir(getEclipseProject().getFolder());

  }
}
