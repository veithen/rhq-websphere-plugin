/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.WorkManagerInfoCO;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

public class WorkManagerThreadPoolMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final String jndiName;
    private final ConfigData<WorkManagerInfoCO> configData;

    public WorkManagerThreadPoolMBeanLocator(String jndiName, ConfigData<WorkManagerInfoCO> configData) {
        super("WebSphere", false);
        this.jndiName = jndiName;
        this.configData = configData;
    }

    @Override
    protected void applyKeyProperties(WebSphereServer server, Map<String,String> props) throws JMException, ConnectorException, InterruptedException {
        String name;
        try {
            name = configData.get().getName();
        } catch (ConfigQueryException ex) {
            // TODO
            throw new RuntimeException(ex);
        }
        if (name == null) {
            throw new JMException("No work manager found for JNDI name " + jndiName);
        }
        props.put("type", "ThreadPool");
        props.put("name", "WorkManager." + name);
    }
    
    // TODO: implement equals and hashCode

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + jndiName + ")";
    }
}
