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
package org.ant4eclipse.jdt.internal.model.project;

import java.util.LinkedList;
import java.util.List;

import org.ant4eclipse.jdt.model.project.RawClasspathEntry;

public class EntryResolver {

  /**
   * @param entryResolver
   * @return A list of resolved paths.
   */
  public static String[] resolveEntries(final Condition condition, final JavaProjectRoleImpl javaProjectRole) {

    final List<String> result = new LinkedList<String>();

    final RawClasspathEntry[] rawClasspathEntries = javaProjectRole.getRawClasspathEntries();

    for (final RawClasspathEntry rawClasspathEntrie : rawClasspathEntries) {

      final String path = condition.resolve(rawClasspathEntrie);

      if (path != null) {
        result.add(path);
      }
    }
    return result.toArray(new String[0]);
  }

  /**
   * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
   */
  public static interface Condition {

    /**
     * @param entry
     * @return The path.
     */
    public String resolve(RawClasspathEntry entry);
  }
}
