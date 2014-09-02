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


$(function () {
    var param = '?';
    var node = QueryString.node;
    if (node) {
        var dropdown = $('#dropdownMenu1');
        dropdown.text(node);
        param = param + 'node=' + node;
        state.node = node;
    }
    var startParam = QueryString['start-time'];
    var endParam = QueryString['end-time'];
    if (startParam && endParam) {
        state.start = startParam;
        state.end = endParam;
        param = param + (param == '?' ? '' : '&') +
            "start-time=" + startParam + "&end-time=" + endParam;
        var buttonSelected = false;
        $('.date-rage-opt').each(function (i, elm) {
            var $elm = $(elm);
            var end = moment(endParam, 'X');
            var start = moment(startParam, 'X');
            if (end.diff(start, $elm.attr('data-unit')) == $elm.attr('data-offset')) {
                $elm.addClass('active');
                buttonSelected = true;
            } else {
                $elm.removeClass('active');
            }
        });
        if (!buttonSelected) {
            $('#reportrange').addClass('active');
        }
    }
    if(param!='?'){
        $('.nav a[href]').attr('href', function (index, href) {
            var i = href.indexOf('?');
            return href.substr(0, i < 0 ? href.length : i) + param;
        });
    }
    console.log(param);
});

