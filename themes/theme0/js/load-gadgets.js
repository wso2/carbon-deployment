$(function(){
    var gadgetUrl = config.gadgetsUrlBase + '/stacked-line-chart/stacked-line-chart.xml';
    var opt = {prefs: {dataSource: caramel.context + '/gadgets/stacked-line-chart/datasource/dataFile1.jag'} };
    UESContainer.renderGadget('request-graph', gadgetUrl, opt);
});
