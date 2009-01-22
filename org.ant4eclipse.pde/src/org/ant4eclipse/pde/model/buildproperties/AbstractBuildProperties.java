/**********************************************************************
 * Copyright (c) 2005-2008 ant4eclipse project team.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nils Hartmann, Daniel Kasmeroglu, Gerd Wuetherich
 **********************************************************************/
package org.ant4eclipse.pde.model.buildproperties;

import org.ant4eclipse.core.Assert;


/**
 * Instances of this class epresents the "common properties" that are used by plugins and features
 * as described in the "Plug-in Development Environment Guide"-Help.
 * 
 * @author Gerd Wuetherich (gerd@gerd-wuetherich.de)
 * @spec Eclipse Help - PDE Guide - "Feature and Plug-in Build Configuration Properties"
 */
public class AbstractBuildProperties {

  /** SELF */
  public static String  SELF              = ".";

  /** CONTEXT_QUALIFIER */
  private static String CONTEXT_QUALIFIER = "context";

  /** NONE_QUALIFIER */
  private static String NONE_QUALIFIER    = "none";

  /**
   * indicates that the build script is hand-crafted as opposed to automatically generated. Therefore no other value is
   * consulted.
   */
  protected boolean     _custom;

  /** lists files that will included in the binary version of the plug-in being built */
  protected String[]    _binIncludes;

  /** lists files to exclude from the binary build */
  protected String[]    _binExcludes;

  /**
   * when the element version number ends with .qualifier this indicates by which value ".qualifier" must be replaced.
   * The value of the property can either be context, &lt;value&gt; or none. Context will generate a date according to
   * the system date, or use the CVS tags when the built is automated. Value is an actual value. None will remove
   * ".qualifier". If the property is omitted, context is used.
   */
  private String        _qualifier        = CONTEXT_QUALIFIER;

  /**
   * 
   */
  public AbstractBuildProperties() {
    // 
  }

  /**
   * @return
   */
  public boolean isCustom() {
    return _custom;
  }

  public String[] getBinaryExcludes() {
    return _binExcludes;
  }

  public String[] getBinaryIncludes() {
    return _binIncludes;
  }

  /**
   * @return Returns the qualifier.
   */
  public String getQualifier() {
    return _qualifier;
  }

  /**
   * @return
   */
  public boolean isContextQualifier() {
    return isContextQualifer(_qualifier);
  }
  
  public static boolean isContextQualifer(String qualifier) {
    return CONTEXT_QUALIFIER.equals(qualifier);
  }

  /**
   * @return
   */
  public boolean isNoneQualifier() {
    return isNoneQualifier(_qualifier);
  }
  
  public static boolean isNoneQualifier(String qualifier) {
    return NONE_QUALIFIER.equals(qualifier);
  }

  /**
   * @param custom
   */
  void setCustom(boolean custom) {
    _custom = custom;
  }

  void setBinaryExcludes(String[] excludes) {
    Assert.notNull(excludes);

    _binExcludes = excludes;
  }

  void setBinaryIncludes(String[] includes) {
    Assert.notNull(includes);

    _binIncludes = includes;
  }

  /**
   * @param qualifier
   *          The qualifier to set.
   */
  void setQualifier(String qualifier) {
    if (qualifier == null) {
      return;
    }

    _qualifier = qualifier;
  }
}
