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

include('../db.jag');
var helper = require('as-data-util.js');
var sqlStatements = require('sql-statements.json');

function getDataForInfoBoxBarChart(type, conditions) {
    var startTime = helper.parseDate(request.getParameter('start_time'));
    var endTime = helper.parseDate(request.getParameter('end_time'));
    var timeDiff = 0;
    var i, len;
    var sql;
    var results;
    var arrList = [];

    if (request.getParameter('start_time') != null && request.getParameter('end_time') != null) {
        timeDiff = Math.abs((endTime.getTime() - startTime.getTime()) / 86400000);
    } else {
        timeDiff = 1;
    }

    var selectStatement = 'SUM';
    if (type == 'averageResponseTime') {
        selectStatement = 'AVG';
    }

    if (timeDiff > 1200) {
        sql = helper.formatSql(sqlStatements.infoBoxGreaterThan1200Days, [selectStatement,
            type, conditions[0]]);
    } else if (timeDiff > 90) {
        sql = helper.formatSql(sqlStatements.infoBoxGreaterThan90Days, [selectStatement,
            type, conditions[0]]);
    } else if (timeDiff > 30) {
        sql = helper.formatSql(sqlStatements.infoBoxGreaterThan30Days, [selectStatement,
            type, conditions[0]]);
    } else if (timeDiff > 1) {
        sql = helper.formatSql(sqlStatements.infoBoxGreaterThan1Day, [selectStatement,
            type, conditions[0]]);
    } else if (timeDiff <= 1) {
        sql = helper.formatSql(sqlStatements.infoBoxLessThan1Day, [selectStatement,
            type, conditions[0]]);
    }

    results = executeQuery(sql, conditions[1]);

    for (i = 0, len = results.length; i < len; i++) {
        var tempData = [];
        tempData[0] = i;
        tempData[1] = results[i]['value'];
        tempData[2] = results[i]['time'] + ' : ' + results[i]['value'];
        arrList.push(tempData);
    }
    return arrList;
}

function getInfoBoxRequestStat(conditions) {
    var output = {};
    var sql = helper.formatSql(sqlStatements.infoBoxRequest, [conditions[0]]);
    var results = executeQuery(sql, conditions[1])[0];

    output['title'] = 'Total Requests';
    output['measure_label'] = 'Per min';

    if (results['totalRequest'] != null) {
        output['total'] = results['totalRequest'];
        output['max'] = results['maxRequest'];
        output['avg'] = Math.round(results['avgRequest']);
        output['min'] = results['minRequest']
    } else {
        output['total'] = output['max'] = output['avg'] = output['min'] = 'N/A';
    }
    output['graph'] = getDataForInfoBoxBarChart('averageRequestCount', conditions);
    print(output);
}

function getInfoBoxResponseStat(conditions) {
    var output = {};

    var sql = helper.formatSql(sqlStatements.infoBoxResponse, [conditions[0]]);
    var results = executeQuery(sql, conditions[1])[0];
    output['title'] = 'Response Time';
    output['measure_label'] = 'ms';

    if (results['maxResponse'] != null) {
        output['max'] = results['maxResponse'];
        output['avg'] = Math.round(results['avgResponse']);
        output['min'] = results['minResponse'];
    } else {
        output['max'] = output['avg'] = output['min'] = 'N/A';
    }
    output['graph'] = getDataForInfoBoxBarChart('averageResponseTime', conditions);
    print(output);
}


function getInfoBoxSessionStat(conditions) {
    var output = {};

    var sql = helper.formatSql(sqlStatements.infoBoxSession, [conditions[0]]);
    var results = executeQuery(sql, conditions[1])[0];
    output['title'] = 'Session';

    if (results['totalSession'] != null) {
        output['total'] = results['totalSession'];
        output['avg'] = Math.round(results['avgSession']);
    } else {
        output['total'] = output['avg'] = 'N/A';
    }
    print(output);
}

function getInfoBoxErrorStat(conditions) {
    var output = {};

    var sql = helper.formatSql(sqlStatements.infoBoxError, [conditions[0]]);
    var results = executeQuery(sql, conditions[1])[0];
    output['title'] = 'Errors';

    if (results['totalError'] != null) {
        output['total'] = results['totalError'];
        output['percentage'] = results['percentageError'].toFixed(2) + '\x25';
    } else {
        output['total'] = output['percentage'] = 'N/A';
    }
    print(output);
}