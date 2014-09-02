var QueryString = function () {
    var query_string = {};
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (typeof query_string[pair[0]] === "undefined") {
            query_string[pair[0]] = pair[1];
        } else if (typeof query_string[pair[0]] === "string") {
            var arr = [ query_string[pair[0]], pair[1] ];
            query_string[pair[0]] = arr;
        } else {
            query_string[pair[0]].push(pair[1]);
        }
    }
    return query_string;
}();

var renderAll = function(){
    var opt, gadgetUrl;
    $('.gadget-holder').each(function (i, el) {
        var $el = $(el);
        if($el.is(':visible') && !Boolean($el.attr('data-rendered'))){
            gadgetUrl = config.gadgetsUrlBase + '/' + $el.attr('data-gadget') + '/' + $el.attr('data-gadget') + '.xml';
            opt = {prefs: {
                dataSource: caramel.context + '/api/as-data.jag',
                startTime: QueryString['start-time'] || undefined,
                endTime: QueryString['end-time'] || undefined,
                node: QueryString['node'] || undefined,
                appStatType: $el.attr('data-type'),
                appname: appname || ''
            } };
            UESContainer.renderGadget($el.attr('id'), gadgetUrl, opt);
            $el.attr('data-rendered',true);
        }
    });
};

$(function(){
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        renderAll();
    });
    renderAll();
});
