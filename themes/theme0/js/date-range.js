//date picker
jQuery('#reportrange').daterangepicker(
    {
        startDate: moment().subtract('days', 29),
        endDate: moment()
    },
    function (start, end) {
        jQuery('#reportrange').find('span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
        UESContainer.inlineClient.publish('wso2.gadgets.charts.timeRangeChange', {start: start, end: end});
    });
