$(function(){
    var gadgetUrl = config.gadgetsUrlBase + '/stacked-line-chart/stacked-line-chart.xml';
//    var opt = {prefs: {dataSource: caramel.context + '/gadgets/stacked-line-chart/datasource/dataFile1.jag'} };
    var opt = {prefs: {dataSource: caramel.context + '/api/as-data.jag?action=summery'} };
    UESContainer.renderGadget('request-graph', gadgetUrl, opt);
});
