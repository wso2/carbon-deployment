$(function(){
//    var opt = {prefs: {dataSource: caramel.context + '/gadgets/stacked-line-chart/datasource/dataFile1.jag'} };
    var gadgetUrl = config.gadgetsUrlBase + '/stacked-line-chart/stacked-line-chart.xml';
    var opt;
    var gadgetTypes = {'request-graph': 'request', 'response-graph': 'response', 'error-graph': 'error'};
    for (var gadgetElmId in gadgetTypes) {
        opt = {prefs: {dataSource: caramel.context + '/api/as-data.jag'} };
        opt.prefs.appStatType = gadgetTypes[gadgetElmId];
        UESContainer.renderGadget(gadgetElmId, gadgetUrl, opt);
    }
});
