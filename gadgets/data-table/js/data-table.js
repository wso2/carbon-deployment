var pref = new gadgets.Prefs();

var url = pref.getString("dataSource");

function fetchData(startTime, endTime) {
    var url = pref.getString("dataSource");

    var data = {
        start_time: startTime,
        end_time: endTime,
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
    var headings = [];

    for(var i = 0; i < tableHeadings.length; i++){
        headings.push({"title": tableHeadings[i]});
    }

    $('#placeholder').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="table" style="width: 100%"></table>');

    $('#table').dataTable({
        "data": tableData,
        "order": [orderColumn],
        "columns" : headings
    });
}

$(document).ready(function () {
    fetchData();
});


gadgets.HubSettings.onConnect = function () {

    gadgets.Hub.subscribe('wso2.gadgets.charts.timeRangeChange',
        function (topic, data, subscriberData) {
            fetchData(data.start.format('YYYY-MM-DD HH:mm'), data.end.format('YYYY-MM-DD HH:mm'))
        }
    );
};

