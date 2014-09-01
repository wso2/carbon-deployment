var pref = new gadgets.Prefs();
var values = null;

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
            fetchData(data.start.format('YYYY-MM-DD HH:mm'), data.end.format('YYYY-MM-DD HH:mm'))
        }
    );
};

