function publishTimeRange(startTime, endTime) {
    UESContainer.inlineClient.publish('wso2.gadgets.charts.timeRangeChange', {start: startTime, end: endTime});
}

function publishIpSelection(ip) {
    UESContainer.inlineClient.publish('wso2.gadgets.charts.ipChange', ip);
}