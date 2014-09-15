var pref = new gadgets.Prefs();
var node = pref.getString("node") || undefined;
var start = pref.getString("startTime") || undefined;
var end = pref.getString("endTime") || undefined;

var url = pref.getString("dataSource");

function fetchData(startTime, endTime) {
    var url = pref.getString("dataSource");

    var data = {
        start_time: start,
        end_time: end,
        node: node,
        action: pref.getString("appStatType")
    };
    var appname = pref.getString("appname");
    if (appname != "") {
        data.appname = appname;
    }
    $.ajax({
        url: url,
        type: "GET",
        dataType: "json",
        data: data,
        success: onDataReceived
    });
}

function onDataReceived(data) {
    var tableData = data.data;
    var tableHeadings = data.headings;
    var orderColumn = data.orderColumn;
    var headings = "<thead><tr>";
    var rowSpan = 1;
    var applist = data.applist || undefined;

    $('#placeholder').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="table" style="width: 100%"></table>');

    for (var i = 0; i < tableHeadings.length; i++) {
        if (tableHeadings[i] instanceof Object) {
            rowSpan = 2;
            break;
        }
    }

    for (var i = 0; i < tableHeadings.length; i++) {
        if (typeof(tableHeadings[i]) == "string") {
            headings += "<th rowspan='" + rowSpan + "'>";
            headings += tableHeadings[i];
        } else {
            headings += "<th colspan='" + tableHeadings[i]["sub"].length + "'>";
            headings += tableHeadings[i]["parent"];
        }
        headings += "</th>";
    }

    headings += "</tr>";

    if (rowSpan > 1) {
        headings += "<tr>";
        for (var i = 0; i < tableHeadings.length; i++) {
            if (tableHeadings[i] instanceof Object) {
                var subHeadings = tableHeadings[i]["sub"];
                for (var j = 0; j < subHeadings.length; j++) {
                    headings += "<th>" + subHeadings[j] + "</th>"
                }
            }
        }
        headings += "</tr>";
    }

    headings += "</thead>";

    $("#table").html(headings);

    var dataTableOptions = new Object();

    dataTableOptions["data"] = tableData;
    dataTableOptions["order"] = [orderColumn];

    if(!applist){
        dataTableOptions["aoColumns"] = [{ "sWidth": "60%" }, { "sWidth": "20%" }, { "sWidth": "20%" }];
    }

    var $table = $('#table');
    var table = $table.dataTable(dataTableOptions);

    if(applist){
        $('#table tbody').on('click', 'tr', function(){
            if($(this).hasClass('selected')){
                $(this).removeClass('selected');
            } else{
                var param = '';
                if (node) {
                    param = 'node=' + node;
                }
                if (start  && end ) {

                    param = param + (param == '' ? '' : '&')  +
                        "start-time=" + moment(start,'YYYY-MM-DD HH:mm').format('X') +
                        "&end-time=" + moment(end,'YYYY-MM-DD HH:mm').format('X');
                }

                var webapp = table.fnGetData(this)[0];
                table.$('tr.selected').removeClass('selected');
                $(this).addClass('selected');
                var webappUrl = parent.window.location.origin + parent.window.location.pathname + 'webapps/' + webapp + '/';
                if (param != '?') {
                    webappUrl = webappUrl + '?' + param;
                }
                parent.window.location.href = webappUrl;

            }
        });
    }
}

$(document).ready(function () {
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

