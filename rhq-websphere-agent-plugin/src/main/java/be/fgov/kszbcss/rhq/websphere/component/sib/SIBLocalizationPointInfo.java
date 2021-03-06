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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.io.Serializable;

public class SIBLocalizationPointInfo implements Serializable {
    private static final long serialVersionUID = -8832735831500379988L;

    private final SIBDestinationType type;
    private final String destinationName;
    
    public SIBLocalizationPointInfo(SIBDestinationType type, String destinationName) {
        this.type = type;
        this.destinationName = destinationName;
    }

    public SIBDestinationType getType() {
        return type;
    }

    public String getDestinationName() {
        return destinationName;
    }
}
