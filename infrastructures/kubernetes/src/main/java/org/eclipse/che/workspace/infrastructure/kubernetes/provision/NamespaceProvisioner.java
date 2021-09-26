/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import io.fabric8.kubernetes.api.model.Namespace;
import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.configurator.NamespaceConfigurator;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.configurator.UserPreferencesConfigurator;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.configurator.UserProfileConfigurator;

/**
 * Provisions the k8s {@link Namespace}. After provisioning, configures the namespace through {@link
 * NamespaceConfigurator}.
 *
 * @author Pavol Baran
 */
public class NamespaceProvisioner {
  private final KubernetesNamespaceFactory namespaceFactory;
  private final UserProfileConfigurator userProfileConfigurator;
  private final UserPreferencesConfigurator userPreferencesConfigurator;

  @Inject
  public NamespaceProvisioner(
      KubernetesNamespaceFactory namespaceFactory,
      UserProfileConfigurator userProfileConfigurator,
      UserPreferencesConfigurator userPreferencesConfigurator) {
    this.namespaceFactory = namespaceFactory;
    this.userProfileConfigurator = userProfileConfigurator;
    this.userPreferencesConfigurator = userPreferencesConfigurator;
  }

  /** Tests for this method are in KubernetesFactoryTest. */
  public KubernetesNamespaceMeta provision(NamespaceResolutionContext namespaceResolutionContext)
      throws InfrastructureException {
    KubernetesNamespace namespace =
        namespaceFactory.getOrCreate(
            new RuntimeIdentityImpl(
                null,
                null,
                namespaceResolutionContext.getUserId(),
                namespaceFactory.evaluateNamespaceName(namespaceResolutionContext)));

    KubernetesNamespaceMeta namespaceMeta =
        namespaceFactory
            .fetchNamespace(namespace.getName())
            .orElseThrow(
                () ->
                    new InfrastructureException(
                        "Not able to find namespace " + namespace.getName()));
    configureNamespace(namespaceResolutionContext);
    return namespaceMeta;
  }

  private void configureNamespace(NamespaceResolutionContext namespaceResolutionContext)
      throws InfrastructureException {
    userProfileConfigurator.configure(namespaceResolutionContext);
    userPreferencesConfigurator.configure(namespaceResolutionContext);
  }
}
