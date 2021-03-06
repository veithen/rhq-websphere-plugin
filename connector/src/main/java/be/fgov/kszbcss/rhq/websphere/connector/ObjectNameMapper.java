/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.connector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class ObjectNameMapper {
    private static final Set<String> nonRoutableDomains = new HashSet<String>(Arrays.asList("java:lang"));
    private static final Set<String> routingPropertyKeys = new HashSet<String>(Arrays.asList("cell", "node", "process"));
    
    private final String cell;
    private final String node;
    private final String process;
    private final Set<ObjectName> nonRoutableMBeans = Collections.synchronizedSet(new HashSet<ObjectName>());
    
    public ObjectNameMapper(String cell, String node, String process) {
        this.cell = cell;
        this.node = node;
        this.process = process;
    }

    public ObjectName toServerObjectName(ObjectName name) {
        if (name.isPropertyPattern() || nonRoutableDomains.contains(name.getDomain())) {
            return name;
        } else if (nonRoutableMBeans.contains(name)) {
            return name;
        } else {
            Hashtable<String,String> newProps = new Hashtable<String,String>(name.getKeyPropertyList());
            newProps.put("cell", cell);
            newProps.put("node", node);
            newProps.put("process", process);
            try {
                return new ObjectName(name.getDomain(), newProps);
            } catch (MalformedObjectNameException ex) {
                // It's unlikely that we ever get here
                throw new Error(ex);
            }
        }
    }
    
    public ObjectName toClientObjectName(ObjectName name) {
        if (nonRoutableMBeans.contains(name)) {
            return name;
        } else {
            Hashtable<String,String> props = name.getKeyPropertyList();
            Hashtable<String,String> newProps = new Hashtable<String,String>();
            int routingPropertyKeyCount = 0;
            for (Map.Entry<String,String> prop : props.entrySet()) {
                String key = prop.getKey();
                if (routingPropertyKeys.contains(key)) {
                    routingPropertyKeyCount++;
                } else {
                    newProps.put(key, prop.getValue());
                }
            }
            if (routingPropertyKeyCount == 0) {
                nonRoutableMBeans.add(name);
                return name;
            } else if (routingPropertyKeyCount == 3) {
                try {
                    return new ObjectName(name.getDomain(), newProps);
                } catch (MalformedObjectNameException ex) {
                    // It's unlikely that we ever get here
                    throw new Error(ex);
                }
            } else {
                throw new IllegalArgumentException("Expected exactly 3 routing key properties");
            }
        }
    }
    
    public Set<ObjectName> toClientObjectNames(Set<ObjectName> names) {
        Set<ObjectName> result = new HashSet<ObjectName>();
        for (ObjectName name : names) {
            result.add(toClientObjectName(name));
        }
        return result;
    }
}
