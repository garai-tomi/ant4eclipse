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
package org.ant4eclipse.pde.tools.target;

import java.io.File;

import org.ant4eclipse.core.service.ServiceRegistry;
import org.ant4eclipse.platform.model.resource.Workspace;

/**
 * <p>
 * The target platform factory can be used to get an instance of type TargetPlatform. The created instances are stored
 * in a map.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 * @author Nils Hartmann (nils@nilshartmann.net)
 */
public interface TargetPlatformRegistry {

  /**
   * <p>
   * Returns an instance of TargetPlatform with the given targetLocation and workspace.
   * <p>
   * 
   * @param workspace
   *          the workspace which contains the projects to build if any
   * @param targetLocation
   *          the location of the platform against which the workspace plugins will be compiled and tested. May be null.
   *          If null, only plugins from the workspace will be used.
   * 
   * @return the TargetPlatform
   */
  public TargetPlatform getInstance(final Workspace workspace, final File[] targetLocation,
      TargetPlatformConfiguration targetPlatformConfiguration);

  /**
   * Removes all target platforms from the factory.
   */
  public void clear();

  /**
   * @param id
   */
  public void setCurrent(TargetPlatform targetPlatform);

  /**
   * <p>
   * Returns the current {@link TargetPlatform} instance. If no current {@link TargetPlatformRegistry} is set, an
   * Exception will be thrown.
   * </p>
   * 
   * @return the current Application Platform. If no current platform is in use an exception will be thrown.
   */
  public TargetPlatform getCurrent();

  /**
   * <p>
   * Returns <code>true</code> if a current {@link DmServer} is specified.
   * </p>
   * 
   * @return
   */
  public boolean hasCurrent();

  /**
   */
  static class Helper {

    /**
     * <p>
     * Fetches the {@link TargetPlatformRegistry} instance from the {@link ServiceRegistry}.
     * </p>
     * 
     * @return the registered {@link TargetPlatformRegistry}
     */
    public static TargetPlatformRegistry getRegistry() {
      return (TargetPlatformRegistry) ServiceRegistry.instance().getService(TargetPlatformRegistry.class.getName());
    }
  }
}
