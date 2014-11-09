/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.undertow;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import io.undertow.UndertowOptions;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.access.management.AccessConstraintDefinition;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.wildfly.extension.io.OptionAttributeDefinition;
import org.xnio.Options;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
abstract class ListenerResourceDefinition extends PersistentResourceDefinition {

    protected static final SimpleAttributeDefinition SOCKET_BINDING = new SimpleAttributeDefinitionBuilder(Constants.SOCKET_BINDING, ModelType.STRING)
            .setAllowNull(false)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_BINDING_REF)
            .build();
    protected static final SimpleAttributeDefinition WORKER = new SimpleAttributeDefinitionBuilder(Constants.WORKER, ModelType.STRING)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .setDefaultValue(new ModelNode("default"))
            .build();
    protected static final SimpleAttributeDefinition BUFFER_POOL = new SimpleAttributeDefinitionBuilder(Constants.BUFFER_POOL, ModelType.STRING)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .setDefaultValue(new ModelNode("default"))
            .build();
    protected static final SimpleAttributeDefinition ENABLED = new SimpleAttributeDefinitionBuilder(Constants.ENABLED, ModelType.BOOLEAN)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(true))
            .setAllowExpression(true)
            .build();

    protected static final SimpleAttributeDefinition REDIRECT_SOCKET = new SimpleAttributeDefinitionBuilder(Constants.REDIRECT_SOCKET, ModelType.STRING)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setDefaultValue(new ModelNode("https"))
            .setAllowExpression(false)
            .build();

    protected static final SimpleAttributeDefinition RESOLVE_PEER_ADDRESS = new SimpleAttributeDefinitionBuilder("resolve-peer-address", ModelType.BOOLEAN)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setDefaultValue(new ModelNode(false))
            .setAllowExpression(true)
            .build();


    public static final OptionAttributeDefinition BACKLOG = OptionAttributeDefinition.builder("tcp-backlog", Options.BACKLOG).setAllowExpression(true).build();
    public static final OptionAttributeDefinition RECEIVE_BUFFER = OptionAttributeDefinition.builder("receive-buffer", Options.RECEIVE_BUFFER).setAllowExpression(true).build();
    public static final OptionAttributeDefinition SEND_BUFFER = OptionAttributeDefinition.builder("send-buffer", Options.SEND_BUFFER).setAllowExpression(true).build();
    public static final OptionAttributeDefinition KEEP_ALIVE = OptionAttributeDefinition.builder("tcp-keep-alive", Options.KEEP_ALIVE).setAllowExpression(true).build();
    public static final OptionAttributeDefinition READ_TIMEOUT = OptionAttributeDefinition.builder("read-timeout", Options.READ_TIMEOUT).setAllowExpression(true).build();
    public static final OptionAttributeDefinition WRITE_TIMEOUT = OptionAttributeDefinition.builder("write-timeout", Options.WRITE_TIMEOUT).setAllowExpression(true).build();


    public static final OptionAttributeDefinition MAX_HEADER_SIZE = OptionAttributeDefinition.builder("max-header-size", UndertowOptions.MAX_HEADER_SIZE).setDefaultValue(new ModelNode(UndertowOptions.DEFAULT_MAX_HEADER_SIZE)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition MAX_ENTITY_SIZE = OptionAttributeDefinition.builder(Constants.MAX_POST_SIZE, UndertowOptions.MAX_ENTITY_SIZE).setDefaultValue(new ModelNode(10485760L)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition BUFFER_PIPELINED_DATA = OptionAttributeDefinition.builder("buffer-pipelined-data", UndertowOptions.BUFFER_PIPELINED_DATA).setDefaultValue(new ModelNode(true)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition MAX_PARAMETERS = OptionAttributeDefinition.builder("max-parameters", UndertowOptions.MAX_PARAMETERS).setDefaultValue(new ModelNode(1000)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition MAX_HEADERS = OptionAttributeDefinition.builder("max-headers", UndertowOptions.MAX_HEADERS).setDefaultValue(new ModelNode(200)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition MAX_COOKIES = OptionAttributeDefinition.builder("max-cookies", UndertowOptions.MAX_COOKIES).setDefaultValue(new ModelNode(200)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition ALLOW_ENCODED_SLASH = OptionAttributeDefinition.builder("allow-encoded-slash", UndertowOptions.ALLOW_ENCODED_SLASH).setDefaultValue(new ModelNode(false)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition DECODE_URL = OptionAttributeDefinition.builder("decode-url", UndertowOptions.DECODE_URL).setDefaultValue(new ModelNode(true)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition URL_CHARSET = OptionAttributeDefinition.builder("url-charset", UndertowOptions.URL_CHARSET).setDefaultValue(new ModelNode("UTF-8")).setAllowExpression(true).build();
    public static final OptionAttributeDefinition ALWAYS_SET_KEEP_ALIVE = OptionAttributeDefinition.builder("always-set-keep-alive", UndertowOptions.ALWAYS_SET_KEEP_ALIVE).setDefaultValue(new ModelNode(true)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition MAX_BUFFERED_REQUEST_SIZE = OptionAttributeDefinition.builder("max-buffered-request-size", UndertowOptions.MAX_BUFFERED_REQUEST_SIZE).setDefaultValue(new ModelNode(16384)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition RECORD_REQUEST_START_TIME = OptionAttributeDefinition.builder("record-request-start-time", UndertowOptions.RECORD_REQUEST_START_TIME).setDefaultValue(new ModelNode(false)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition ALLOW_EQUALS_IN_COOKIE_VALUE = OptionAttributeDefinition.builder("allow-equals-in-cookie-value", UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE).setDefaultValue(new ModelNode(false)).setAllowExpression(true).build();
    public static final OptionAttributeDefinition NO_REQUEST_TIMEOUT = OptionAttributeDefinition.builder("no-request-timeout", UndertowOptions.NO_REQUEST_TIMEOUT).setDefaultValue(new ModelNode(-1)).setAllowExpression(true).setMeasurementUnit(MeasurementUnit.MILLISECONDS).build();
    public static final OptionAttributeDefinition REQUEST_PARSE_TIMEOUT = OptionAttributeDefinition.builder("request-parse-timeout", UndertowOptions.REQUEST_PARSE_TIMEOUT).setDefaultValue(new ModelNode(-1)).setAllowExpression(true).setMeasurementUnit(MeasurementUnit.MILLISECONDS).build();


    protected static final Collection ATTRIBUTES;
    protected static final List<AccessConstraintDefinition> CONSTRAINTS = Arrays.asList(UndertowExtension.LISTENER_CONSTRAINT);

    public static final List<OptionAttributeDefinition> LISTENER_OPTIONS = Arrays.asList(MAX_HEADER_SIZE, MAX_ENTITY_SIZE,
            BUFFER_PIPELINED_DATA, MAX_PARAMETERS, MAX_HEADERS, MAX_COOKIES, ALLOW_ENCODED_SLASH, DECODE_URL,
            URL_CHARSET, ALWAYS_SET_KEEP_ALIVE, MAX_BUFFERED_REQUEST_SIZE, RECORD_REQUEST_START_TIME,
            ALLOW_EQUALS_IN_COOKIE_VALUE,
            NO_REQUEST_TIMEOUT,
            REQUEST_PARSE_TIMEOUT);

    public static final List<OptionAttributeDefinition> SOCKET_OPTIONS = Arrays.asList(BACKLOG, RECEIVE_BUFFER, SEND_BUFFER, KEEP_ALIVE, READ_TIMEOUT, WRITE_TIMEOUT);

    static {
        ATTRIBUTES = new LinkedHashSet<AttributeDefinition>(Arrays.asList(SOCKET_BINDING, WORKER, BUFFER_POOL, ENABLED, RESOLVE_PEER_ADDRESS));
        ATTRIBUTES.addAll(LISTENER_OPTIONS);
        ATTRIBUTES.addAll(SOCKET_OPTIONS);
    }

    public ListenerResourceDefinition(PathElement pathElement) {
        super(pathElement, UndertowExtension.getResolver(Constants.LISTENER)
        );
    }

    public Collection<AttributeDefinition> getAttributes() {
        //noinspection unchecked
        return ATTRIBUTES;
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        super.registerAddOperation(resourceRegistration, getAddHandler(), OperationEntry.Flag.RESTART_NONE);
        super.registerRemoveOperation(resourceRegistration, new ListenerRemoveHandler(getAddHandler()), OperationEntry.Flag.RESTART_NONE);
    }

    @Override
    public List<AccessConstraintDefinition> getAccessConstraints() {
        return CONSTRAINTS;
    }

    protected abstract ListenerAdd getAddHandler();
}
