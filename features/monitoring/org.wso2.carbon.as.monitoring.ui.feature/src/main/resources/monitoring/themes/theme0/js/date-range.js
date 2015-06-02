/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
