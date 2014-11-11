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

var QueryString = function () {
    var query_string = {};
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (typeof query_string[pair[0]] === "undefined") {
            query_string[pair[0]] = pair[1];
        } else if (typeof query_string[pair[0]] === "string") {
            var arr = [ query_string[pair[0]], pair[1] ];
            query_string[pair[0]] = arr;
        } else {
            query_string[pair[0]].push(pair[1]);
        }
    }
    return query_string;
}();

if (QueryString.node) {
    state.node = QueryString.node
}
if (QueryString['start-time']) {
    state.start = QueryString['start-time']
}
if (QueryString['end-time']) {
    state.end = QueryString['end-time']
}

var updateLinks = function () {
    var param = '?';
    if (state.node) {
        var dropdown = $('#dropdownMenu1');
        dropdown.text(state.node);
        param = param + 'node=' + state.node;
    }
    if (state.start && state.end) {
        param = param + (param == '?' ? '' : '&') +
            "start-time=" + state.start + "&end-time=" + state.end;
        var buttonSelected = false;
        $('.date-rage-opt').each(function (i, elm) {
            var $elm = $(elm);
            var end = moment(state.end, 'X');
            var start = moment(state.start, 'X');
            if (end.diff(start, $elm.attr('data-unit')) == $elm.attr('data-offset')) {
                $elm.addClass('active');
                buttonSelected = true;
            } else {
                $elm.removeClass('active');
            }
        });
        if (!buttonSelected) {
            $('#reportrange').addClass('active_datepicker');
        }else{
            $('#reportrange').removeClass('active_datepicker');
        }
    }
    if (param != '?') {
        $('.in-link').attr('href', function (index, href) {
            var i = href.indexOf('?');
            return href.substr(0, i < 0 ? href.length : i) + param;
        });
        window.history.replaceState(state,'',param);
    }

};

window.onpopstate = function (event) {
    if(event.state){
        state = event.state;
        updateLinks();
        console.log('pop' + event.state);
        state.node && publishIpSelection(state.node);
        state.start &&  publishTimeRange(moment(state.start, 'X'), moment(state.end, 'X'));
    }
//    alert("location: " + document.location + ", state: " + JSON.stringify(event.state));
};

$(function () {
    updateLinks();
});

