$(document).ready(function () {
    var home =  caramel.context ;
    var url =  caramel.context + '/api/as-data.jag';
    $.ajax({
        url: url,
        type: "GET",
        dataType: "json",
        data: {
            "action": "node-list"
        },
        success: function(data){
            var serverList = document.getElementById('server-list');
            for(var i = 0; i < data.length; i++){
                var link = document.createElement('a');
                var item = document.createElement('li');

                link.role = "menuitem";
                link.href = "#";
                link.innerHTML = data[i];
                item.role = "presentation";
                item.appendChild(link);
                serverList.appendChild(item);
            }
        }
    });


    $('#server-list').on('click', 'li', function (event) {
        var $target = $(event.currentTarget);
        var ip = $target.text();
        $('#dropdownMenu1').text(ip);
        UESContainer.inlineClient.publish('wso2.gadgets.charts.ipChange',ip);
        state.node = ip;

        $('.in-link').attr('href', function (index, href) {
            var param = '?node=' + ip ;
            if (state.start) {
                param = param + "&start-time=" + state.start + "&end-time=" + state.end;
            }
            var i = href.indexOf('?');
            return href.substr(0, i < 0 ? href.length : i) + param;
        });

        return true;
    });

    $('#home').attr('href', home);
});
