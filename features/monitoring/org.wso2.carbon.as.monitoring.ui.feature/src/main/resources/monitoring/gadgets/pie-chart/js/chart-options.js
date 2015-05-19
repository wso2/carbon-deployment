(function () {

    chartConfigs = function () {
        return {
            "legend": {
                "show": false,
                "labelFormatter": null,
                "labelBoxBorderColor": "#E9E8E8",
                "noColumns": 1,
                "position": "ne",
                "backgroundColor": "#FFFFFF",
                "backgroundOpacity": 0.8,
                "container": null
            },
            "series": {
                "pie": {
                    "show": true,
                    "radius": 0.75,
                    "innerRadius": 0,
                    "label": {
                        "show": true
                    }
                }
            },
            "colors": ["#005a32", "#238b45", "#41ab5d", "#74c476", "#a1d99b", "#c7e9c0", "#edf8e9"],
            "grid": {
                "show": true,
                "aboveData": false,
                "color": "#000000",
                "backgroundColor": "#333333",
                "labelMargin": 8,
                "axisMargin": null,
                "markings": null,
                "borderWidth": 0.5,
                "borderColor": "#FFFFFF",
                "minBorderMargin": null,
                "clickable": false,
                "hoverable": false,
                "autoHighlight": true,
                "mouseActiveRadius": 0.1
            },
            "pan": {
                "interactive": true
            },
            "zoom": {
                "interactive": true
            },
            "selection": {
                "mode": null
            }

        };
    };
}());

