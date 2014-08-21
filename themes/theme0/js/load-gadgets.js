$(function(){
    var opt, gadgetUrl;
    $('.gadget-holder').each(function (i, el) {
        var $el = $(el);
        gadgetUrl = config.gadgetsUrlBase + '/' + $el.attr('data-gadget') + '/' + $el.attr('data-gadget') + '.xml';
        opt = {prefs: {dataSource: caramel.context + '/api/as-data.jag'} };
        opt.prefs.appStatType = $el.attr('data-type');
        UESContainer.renderGadget($el.attr('id'), gadgetUrl, opt);
    });
});
