function formatDate(d) {
    var date = new Date(d);
    var year = date.getFullYear();
    var month = ('0' + (date.getMonth() + 1)).slice(-2);
    var day = ('0' + date.getDate()).slice(-2);
    var hour = ('0' + date.getHours()).slice(-2);
    var minute = ('0' + date.getMinutes()).slice(-2);
    return year + '-' + month + '-' + day + ' ' + hour + ':' + minute;
}

/*
 This method returns the most significant results of the resultset.
 "significantRecordCount" is the number of most significant results to be returned.
 The rest, if any, will be aggregated under the name "groupName".
 */
function getShrinkedResultset(resultset, significantRecordCount, groupName) {
    var shrinkedResultset;
    var total = 0;
    var percentage = 0;
    var i;

    if (significantRecordCount >= resultset.length) {
        return resultset;
    }

    shrinkedResultset = resultset.slice(0, significantRecordCount);

    for (i = significantRecordCount; i < resultset.length; i++) {
        total = total + resultset[i]['request_count'];
        percentage = percentage + resultset[i]['percentage_request_count'];
    }

    shrinkedResultset.push({
        'request_count': total,
        'percentage_request_count': percentage.toFixed(2),
        'name': groupName
    });
    return shrinkedResultset;
}

/*
 This method returns the dataset to suit to be displayed in a datatable.
 */
function getTabularData(dataSet, columns, sortColumn) {
    var i, len;
    var key;
    var dataArray = [];
    var row;
    if (dataSet == null) {
        return;
    }

    for (i = 0, len = dataSet.length; i < len; i++) {
        row = [];
        for (key in dataSet[i]) {
            row.push(dataSet[i][key]);
        }
        dataArray.push(row);
    }
    return {'data': dataArray, 'headings': columns, 'orderColumn': [sortColumn, 'desc']};
}

function parseDate(input) {
    var parts;
    if(!input){
        return;
    }
    var p = input.split(' ');
    input = p[0];
    parts = input.split('-');

    // new Date(year, month [, day [, hours[, minutes[, seconds[, ms]]]]])
    return new Date(parts[0], parts[1] - 1, parts[2]); // Note: months are 0-based
}

/*
 This method fill the placeholders of the sql statements with the arguments passed and return the
 completed sql statement.
 */
function formatSql(sql, arguments){
    var i, len;
    var formattedSql = sql;

    for (i = 0, len = arguments.length; i < len; i++) {
        formattedSql = formattedSql.replace(RegExp('\\{' + (i+1) + '\\}','g'), arguments[i]);
    }
    return formattedSql;
}
