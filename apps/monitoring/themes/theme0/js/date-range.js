(function () {
    var $rangeOpt = $('.date-rage-opt');
    var dateRangeCustom = jQuery('#reportrange');
    var timeLabel = dateRangeCustom.find('span');
    var picker;

    var changeRange = function (start, end) {
        var startStr = start.format('MMM D, YYYY');
        var endStr = end.format('MMM D, YYYY');
        timeLabel.text(startStr + ' - ' + endStr);
        publishTimeRange(start, end);
        var param = "?start-time=" + start.unix() + "&end-time=" + end.unix();
        if (state.node) {
            param = param + '&node=' + state.node;
        }
        $('.in-link').attr('href', function (index, href) {
            var i = href.indexOf('?');
            return href.substr(0, i < 0 ? href.length : i) + param;
        });
        state.start = start.unix();
        state.end = end.unix();

        history.pushState(state, '', param);
    };

    var start = state.start ?  moment(state.start,'X') : moment().subtract('days', 1);
    var end  = state.end ?  moment(state.end,'X') : moment();

    timeLabel.text(start.format('MMM D, YYYY') + ' - ' + end.format('MMM D, YYYY'));
    dateRangeCustom.daterangepicker(
        {
            startDate: start,
            endDate: end
        },
        function (start, end) {
            dateRangeCustom.addClass('active_datepicker');
            $rangeOpt.removeClass('active');
            changeRange(start, end);
        });
    picker = dateRangeCustom.data('daterangepicker');

    $rangeOpt.on('click', function () {
        var $this = $(this);

        dateRangeCustom.removeClass('active_datepicker');
        $rangeOpt.removeClass('active');
        $this.addClass('active');
        var end = moment();
        var start = moment().subtract($this.attr('data-unit'), $this.attr('data-offset'));
        picker.setStartDate(start);
        picker.setEndDate(end);

        changeRange(start,end);
    });

})();