/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

include('../db.jag');
var helper = require('as-data-util.js');
var sqlStatements = require('sql-statements.json');

// type: [table-name, field-name]
var dbMapping = {
    'browser': ['USER_AGENT_FAMILY', 'userAgentFamily'],
    'os': ['OPERATING_SYSTEM', 'operatingSystem'],
    'device-type': ['DEVICE_TYPE', 'deviceCategory']
};

function getTechnologyStatData(conditions, type) {
    var dbEntries = dbMapping[type];
    var sql = helper.formatSql(sqlStatements.technology, [dbEntries[1], dbEntries[0],
        conditions[0]]);
    return executeQuery(sql, conditions[1]);
}

function getTechnologyStat(conditions, type, visibleNumbers, groupName) {
    var dataObject = {};
    var i, len;
    var row;
    var series;
    var data;
    var chartOptions = {};

    var results = getTechnologyStatData(conditions, type);

    var shrinkedResults = helper.getShrinkedResultset(results, visibleNumbers, groupName);

    for (i = 0, len = shrinkedResults.length; i < len; i++) {
        row = shrinkedResults[i];
        series = 'series' + i;
        data = {'label': row['name'], 'data': row['request_count']};
        dataObject[series] = data;
    }

    print([dataObject, chartOptions]);
}

function getTechnologyTubularStat(conditions, type, tableHeadings, sortColumn) {
    print(helper.getTabularData(getTechnologyStatData(conditions, type), tableHeadings, sortColumn));
}

function getHttpStatusStatData(conditions) {
    var sql = helper.formatSql(sqlStatements.httpStatus,[conditions[0]]);
    return executeQuery(sql, conditions[1]);
}

function getHttpStatusStat(conditions) {
    var dataArray = [];
    var ticks = [];
    var i, len;
    var row;
    var opt;
    var results = getHttpStatusStatData(conditions);

    for (i = 0, len = results.length; (i < len) && (i < 5); i++) {
        row = results[i];
        dataArray.push([i, row['request_count']]);
        ticks.push([i, row['name']]);
    }

    opt = require('/gadgets/bar-chart/config/chart-options.json');
    opt.xaxis.ticks = ticks;
    opt.xaxis.axisLabel = 'Top 5 HTTP Response codes';
    opt.yaxis.axisLabel = 'Number of requests';
    print([
        {'series1': {'label': 's', 'data': dataArray}},
        opt
    ]);
}

function getHttpStatusTabularStat(conditions, tableHeadings, sortColumn) {
    print(helper.getTabularData(getHttpStatusStatData(conditions), tableHeadings, sortColumn));
}