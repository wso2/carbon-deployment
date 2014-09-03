var pref = new gadgets.Prefs();
var values = null;
var node = pref.getString("node") || undefined;
var start = pref.getString("startTime") || undefined;
var end = pref.getString("endTime") || undefined;

var url = pref.getString("dataSource");

function fetchData() {
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
    values = data;
    var map = $('#world-map').vectorMap('get', 'mapObject');
    map.series.regions[0].setValues(values);

    if ($.isEmptyObject(values)) {
        map.series.regions[0].clear();
    }
}

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

