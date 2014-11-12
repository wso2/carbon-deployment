/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var intervalId;

function registerSubscriber() {

    if (!UESContainer.inlineClient) {
        return;
    }

    UESContainer.inlineClient.subscribe('wso2.as.http.dashboard.webapp.url',
        // redirects the dashboard based on the selected webapp
        function (topic, data, subscriberData) {
            var url = parent.window.location.origin + parent.window.location.pathname + 'webapps/' + data + '/';
            window.location.href = url;
        }
    );

    window.clearInterval(intervalId);
}

$(function () {
    intervalId = window.setInterval(registerSubscriber, 100);
});

