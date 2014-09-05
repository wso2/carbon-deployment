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
            for (var i = 0; i < data.length; i++) {
                if (data[i] instanceof Object) {
                    appendElementsToServerList(serverList, data[i]["groupName"], true);
                    var elements = data[i]["elements"];
                    for(var j = 0; j < elements.length; j++){
                        appendElementsToServerList(serverList, elements[j], false);
                    }
                    continue;
                }
                appendElementsToServerList(serverList, data[i], false);
            }
        }
    });


    $('#server-list').on('click', 'li', function (event) {
        var $target = $(event.currentTarget);
        var ip = $target.text();
        $('#dropdownMenu1').text(ip);
        publishIpSelection(ip);
        state.node = ip;

        var param = '?node=' + ip ;
        if (state.start) {
            param = param + "&start-time=" + state.start + "&end-time=" + state.end;
        }
        $('.in-link').attr('href', function (index, href) {
            var i = href.indexOf('?');
            return href.substr(0, i < 0 ? href.length : i) + param;
        });
        history.pushState(state, '', param);

        return true;
    });

    $('#home').attr('href', home);
});

function appendElementsToServerList(serverList, element, header) {
    var item = document.createElement('li');
    item.role = "presentation";
    if (header) {
        item.classList.add('dropdown-header');
        item.innerHTML = element;
        var divider = document.createElement('li');
        divider.role = "presentation";
        divider.classList.add('divider');
        serverList.appendChild(divider);
    }
    else{
        var link = document.createElement('a');
        link.innerHTML = element;
        link.role = "menuitem";
        item.appendChild(link);
    }
    serverList.appendChild(item);
}