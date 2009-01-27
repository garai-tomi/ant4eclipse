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
package org.ant4eclipse.jdt.internal.tools.classpathentry;

import org.ant4eclipse.jdt.model.ClasspathEntry;
import org.ant4eclipse.jdt.model.project.RawClasspathEntry;
import org.ant4eclipse.jdt.tools.container.ClasspathResolverContext;

/**
 * <p>
 * Implements a {@link ClasspathEntryResolver} to resolve class path entries of type
 * {@link RawClasspathEntry#CPE_OUTPUT}. Due to the fact the the output locations of eclipse java project are already
 * resolved by the {@link SourceClasspathEntryResolver}, this {@link ClasspathEntryResolver} implements a simple NOOP.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class OutputClasspathEntryResolver extends AbstractClasspathEntryResolver {

  /**
   * @see org.ant4eclipse.jdt.internal.tools.classpathentry.ClasspathEntryResolver#canResolve(org.ant4eclipse.jdt.model.ClasspathEntry)
   */
  public boolean canResolve(final ClasspathEntry entry) {
    // only suitable for raw class path entries of kind CPE_OUTPUT
    return isRawClasspathEntryOfKind(entry, RawClasspathEntry.CPE_OUTPUT);
  }

  /**
   * @see org.ant4eclipse.jdt.internal.tools.classpathentry.ClasspathEntryResolver#resolve(org.ant4eclipse.jdt.model.ClasspathEntry,
   *      org.ant4eclipse.jdt.tools.container.ClasspathResolverContext)
   */
  public void resolve(final ClasspathEntry entry, final ClasspathResolverContext context) {
    // nothing to do here - handled within the Source Handler
  }
}