/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.webservices.deployers;

import static org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION;
import static org.jboss.as.webservices.util.ASHelper.getRequiredAttachment;

import java.util.List;

import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.webservices.injection.WSComponentDescription;
import org.jboss.as.webservices.service.EndpointService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class WSComponentDescriptionFactory implements DeploymentUnitProcessor {

    private static final Logger logger = Logger.getLogger(WSComponentDescriptionFactory.class);

    private final DotName[] dotNames;

    protected WSComponentDescriptionFactory(final DotName... dotNames) {
        this.dotNames = dotNames;
    }

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentTypeMarker.isType(DeploymentType.EAR, unit)) {
            return;
        }

        final CompositeIndex index = unit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (index == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Skipping WS annotation processing since no composite annotation index found in unit: " + unit);
            }
        } else {
            for (final DotName dotName : dotNames) {
                final List<AnnotationInstance> wsAnnotations = index.getAnnotations(dotName);
                if (!wsAnnotations.isEmpty()) {
                    for (final AnnotationInstance wsAnnotation : wsAnnotations) {
                        final AnnotationTarget target = wsAnnotation.target();
                        if (target instanceof ClassInfo) {
                            final ClassInfo classInfo = (ClassInfo) target;
                            if (matches(classInfo, index)) {
                                processAnnotation(unit, classInfo, wsAnnotation, index);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit unit) {
        // does nothing
    }

    protected abstract void processAnnotation(final DeploymentUnit unit, final ClassInfo classInfo, final AnnotationInstance wsAnnotation, final CompositeIndex compositeIndex) throws DeploymentUnitProcessingException;

    protected abstract boolean matches(final ClassInfo classInfo, final CompositeIndex index);

    protected static ComponentDescription createComponentDescription(final DeploymentUnit unit, final String componentName, final String componentClassName, final String dependsOnEndpointClassName) {
        final EEModuleDescription moduleDescription = getRequiredAttachment(unit, EE_MODULE_DESCRIPTION);
        ComponentDescription componentDescription = moduleDescription.getComponentByName(componentName);

        if (componentDescription == null) {
            // register WS component
            componentDescription = new WSComponentDescription(componentName, componentClassName, moduleDescription, unit.getServiceName());
            moduleDescription.addComponent(componentDescription);
            // registering WS dependency
            final ServiceName endpointServiceName = EndpointService.getServiceName(unit, dependsOnEndpointClassName);
            componentDescription.addDependency(endpointServiceName, ServiceBuilder.DependencyType.REQUIRED);
        }

        return componentDescription;
    }

}
