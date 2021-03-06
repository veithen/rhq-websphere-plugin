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
package be.fgov.kszbcss.rhq.websphere.connector.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.kszbcss.rhq.websphere.util.PIDChangeTracker;
import be.fgov.kszbcss.rhq.websphere.util.PIDWatcher;

import com.ibm.websphere.management.AdminClient;

/**
 * Manages JMX notification listeners registered on a given WebSphere instance. It provides the
 * following services:
 * <ul>
 * <li>If the registration of a listener fails, it will be retried later (until registration
 * succeeds).
 * <li>If the unregistration of a listener fails, it will be retried later (until unregistration
 * succeeds).
 * <li>It periodically queries the PID of the WebSphere instance. If a PID change is detected, then
 * all listeners are re-registered.
 * </ul>
 */
public class NotificationListenerManager {
    private static final Logger log = LoggerFactory.getLogger(NotificationListenerManager.class);
    
    private final AdminClient adminClient;
    private final PIDChangeTracker pidChangeTracker;
    private final List<NotificationListenerRegistration> registrations = new ArrayList<NotificationListenerRegistration>();
    private Timer timer;
    
    public NotificationListenerManager(AdminClient adminClient, PIDWatcher pidWatcher) {
        this.adminClient = adminClient;
        pidChangeTracker = pidWatcher.createTracker();
    }

    public NotificationListenerRegistration addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback, boolean extended) {
        NotificationListenerRegistration registration = new NotificationListenerRegistration(this, name, listener, filter, handback, extended);
        registration.update(adminClient);
        synchronized (registrations) {
            registrations.add(registration);
            if (timer == null) {
                log.debug("Starting notification registration timer");
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateRegistrations();
                    }
                }, 60000, 60000);
            }
        }
        return registration;
    }

    void unregister(NotificationListenerRegistration registration) {
        registration.scheduleForUnregistration();
        registration.update(adminClient);
        cleanup();
    }
    
    void updateRegistrations() {
        if (pidChangeTracker.isRestarted()) {
            if (log.isDebugEnabled()) {
                log.debug("PID change occurred; marking all listeners as unregistered");
            }
            synchronized (registrations) {
                for (NotificationListenerRegistration registration : registrations) {
                    registration.markAsUnregistered();
                }
            }
        }
        List<NotificationListenerRegistration> registrationsList;
        // Create a copy of the "registrations" collection; the JMX call may take some time and we don't
        // want to block other threads
        synchronized (registrations) {
            registrationsList = new ArrayList<NotificationListenerRegistration>(registrations);
        }
        for (NotificationListenerRegistration registration : registrationsList) {
            registration.update(adminClient);
        }
        cleanup();
    }
    
    private void cleanup() {
        synchronized (registrations) {
            for (Iterator<NotificationListenerRegistration> it = registrations.iterator(); it.hasNext(); ) {
                if (it.next().isRemoved()) {
                    it.remove();
                }
            }
            if (timer != null && registrations.isEmpty()) {
                log.debug("Stopping notification registration timer");
                timer.cancel();
                timer = null;
            }
        }
    }
}
