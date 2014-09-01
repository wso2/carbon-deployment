(function () {
    var $rangeOpt = $('.date-rage-opt');
    var dateRangeCustom = jQuery('#reportrange');
    var timeLabel = dateRangeCustom.find('span');
    var picker;

    var changeRange = function (start, end) {
        timeLabel.html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
        UESContainer.inlineClient.publish('wso2.gadgets.charts.timeRangeChange', {start: start, end: end});
    };

    dateRangeCustom.daterangepicker(
        {
            startDate: moment().subtract('days', 29),
            endDate: moment()
        },
        function (start, end) {
            dateRangeCustom.addClass('active');
            $rangeOpt.removeClass('active');
            changeRange(start,end);
        });
    picker = dateRangeCustom.data('daterangepicker');

    $rangeOpt.on('click', function () {
        var $this = $(this);

        dateRangeCustom.removeClass('active');
        $rangeOpt.removeClass('active');
        $this.addClass('active');
        var end = moment();
        var start = moment().subtract($this.attr('data-unit'), $this.attr('data-offset'));
        picker.setStartDate(start);
        picker.setEndDate(end);

        changeRange(start,end);
    });

})();
