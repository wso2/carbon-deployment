(function () {
    var onDataReceived = function () {

    };
    $.ajax({
        url: url,
        type: "GET",
        dataType: "json",
        data: {
            start_time: startTime,
            end_time: endTime,
            action: pref.getString("appStatType")
        },
        success: onDataReceived
    });
})();
