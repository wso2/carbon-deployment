var renderAll = function(){
    var opt, gadgetUrl;
    $('.gadget-holder').each(function (i, el) {
        var $el = $(el);
        if($el.is(':visible') && !Boolean($el.attr('data-rendered'))){
            gadgetUrl = config.gadgetsUrlBase + '/' + $el.attr('data-gadget') + '/' + $el.attr('data-gadget') + '.xml';
            opt = {prefs: {
                dataSource: caramel.context + '/api/as-data.jag',
                startTime: moment(state.start, 'X').format('YYYY-MM-DD HH:mm') || undefined,
                endTime: moment(state.end, 'X').format('YYYY-MM-DD HH:mm')  || undefined,
                node: state.node || undefined,
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
