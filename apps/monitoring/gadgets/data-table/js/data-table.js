var pref = new gadgets.Prefs();
var node = pref.getString('node') || undefined;
var start = pref.getString('startTime') || undefined;
var end = pref.getString('endTime') || undefined;

var url = pref.getString('dataSource');

function fetchData(startTime, endTime) {
    var url = pref.getString('dataSource');

    var data = {
        start_time: start,
        end_time: end,
        node: node,
        action: pref.getString('appStatType')
    };
    var appname = pref.getString('appname');
    if (appname != '') {
        data.appname = appname;
    }
    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'json',
        data: data,
        success: onDataReceived
    });
}

function onDataReceived(data) {
    var tableData = data.data;
    var tableHeadings = data.headings;
    var orderColumn = data.orderColumn;
    var applist = data.applist || undefined;
    var table;
    var headings;

    table = '<table cellpadding="0" cellspacing="0" border="0" class="display" id="table" style="width: 100%">';

    headings = getTableHeader(tableHeadings);

    $('#placeholder').html(table + headings + '</table>');

    var dataTableOptions = {};

    dataTableOptions['data'] = tableData;
    dataTableOptions['order'] = [orderColumn];

    if (!applist) {
        dataTableOptions['aoColumns'] = [
            { 'sWidth': '60%' },
            { 'sWidth': '20%' },
            { 'sWidth': '20%' }
        ];
    }

    table = $('#table').dataTable(dataTableOptions);

    if (applist) {
        registerWebappSelect(table);
    }
}

function registerWebappSelect(table) {
    table.find('tbody').on('click', 'tr', function () {
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
        } else {
            var param = '';
            if (node) {
                param = 'node=' + node;
            }
            if (start && end) {

                param = param + (param == '' ? '' : '&') +
                    'start-time=' + moment(start, 'YYYY-MM-DD HH:mm').format('X') +
                    '&end-time=' + moment(end, 'YYYY-MM-DD HH:mm').format('X');
            }

            var webapp = table.fnGetData(this)[0];
            table.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
            var webappUrl = webapp;
            if (param != '?') {
                webappUrl = webappUrl + '?' + param;
            }

            publishRedirectUrl(webappUrl);
        }
    });
}

function getTableHeader(tableHeadings) {
    var headings = '<thead><tr>';
    var rowSpan = 1;
    var i, len;

    for (i = 0, len = tableHeadings.length; i < len; i++) {
        if (tableHeadings[i] instanceof Object) {
            rowSpan = 2;
            break;
        }
    }

    for (i = 0, len = tableHeadings.length; i < len; i++) {
        if (typeof(tableHeadings[i]) == 'string') {
            headings += "<th rowspan='" + rowSpan + "'>";
            headings += tableHeadings[i];
        } else {
            headings += "<th colspan='" + tableHeadings[i]["sub"].length + "'>";
            headings += tableHeadings[i]["parent"];
        }
        headings += '</th>';
    }

    headings += '</tr>';

    if (rowSpan > 1) {
        headings += '<tr>';
        for (i = 0, len = tableHeadings.length; i < len; i++) {
            if (tableHeadings[i] instanceof Object) {
                var subHeadings = tableHeadings[i]['sub'];
                for (j = 0, len2 = subHeadings.length; j < len2; j++) {
                    headings += '<th>' + subHeadings[j] + '</th>'
                }
            }
        }
        headings += '</tr>';
    }

    headings += '</thead>';

    return headings;
}

function publishRedirectUrl(url){
         gadgets.Hub.publish('wso2.as.http.dashboard.webapp.url', url);
}

$(function () {
    fetchData();
});

gadgets.HubSettings.onConnect = function () {

    gadgets.Hub.subscribe('wso2.gadgets.charts.timeRangeChange',
        function (topic, data, subscriberData) {
            start = data.start.format('YYYY-MM-DD HH:mm');
            end = data.end.format('YYYY-MM-DD HH:mm');
            fetchData();
        }
    );

    gadgets.Hub.subscribe('wso2.gadgets.charts.ipChange',
        function (topic, data, subscriberData) {
            node = data;
            fetchData();
        }
    );
};

