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

    if($.isEmptyObject(data)){
        $('#world-map').html("<div class='no-data'>No data available for selected options..!</div>");
        return;
    }

    $('#world-map').vectorMap({
        map: 'world_mill_en',
        series: {
            regions: [{
                values: null,
                scale: ['#C8EEFF', '#0071A4'],
                normalizeFunction: 'polynomial'
            }]
        },
        onRegionLabelShow: function(e, el, code){
            var request_count_tooltip = 0;;

            if(values[code]){
                request_count_tooltip = values[code];
            }
            el.html('Country: ' + el.html()+ ' (total request count: ' + request_count_tooltip + ')');
        }
    });

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

