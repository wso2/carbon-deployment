/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.webapp.mgt.session;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is use to debug hazelcast distributed map and this can be use to develop map specific
 * operations, As of now this class is use to debug distributed map in session replication feature.
 */
public class MapEntryListener implements EntryListener<String, Object> {

	private static Log LOGGER = LogFactory.getLog(MapEntryListener.class);

	@Override
	public void entryAdded(EntryEvent<String, Object> longPersonEntryEvent) {
		LOGGER.debug("*Session Added Entry " + longPersonEntryEvent);
	}

	@Override
	public void entryRemoved(EntryEvent<String, Object> longPersonEntryEvent) {
		LOGGER.debug("*Session Removed Entry " + longPersonEntryEvent);
	}

	@Override
	public void entryUpdated(EntryEvent<String, Object> longPersonEntryEvent) {
		LOGGER.debug("*Session Updated Entry " + longPersonEntryEvent);
	}

	@Override
	public void entryEvicted(EntryEvent<String, Object> longPersonEntryEvent) {
		LOGGER.debug("*Session Evicted Entry " + longPersonEntryEvent);
	}
}
