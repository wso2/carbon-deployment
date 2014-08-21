$(function(){
    var gadgetUrl = config.gadgetsUrlBase + '/stacked-line-chart/stacked-line-chart.xml';
    var opt;
    $('.gadget-holder').each(function (i, el) {
        var $el = $(el);
        opt = {prefs: {dataSource: caramel.context + '/api/as-data.jag'} };
        opt.prefs.appStatType = $el.attr('data-type');
        UESContainer.renderGadget($el.attr('id'), gadgetUrl, opt);
    });
});
