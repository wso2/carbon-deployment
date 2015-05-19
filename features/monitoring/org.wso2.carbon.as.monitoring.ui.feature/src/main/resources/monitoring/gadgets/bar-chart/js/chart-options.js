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
                "shadowSize": 1,
                "bars": {
                    "show": true,
                    "barWidth": 0.13,
                    "order": 1
                }
            },
            "colors": ["#2b8cbe", "#fc8d59", "#F5A00C", "#061DA2", "#C921E6", "#21CCE6", "#55FFBB"],
            "grid": {
                "show": true,
                "aboveData": false,
                "color": "#000000",
                "backgroundColor": "#ECECEC",
                "labelMargin": 8,
                "axisMargin": null,
                "markings": null,
                "borderWidth": 0.2,
                "borderColor": "#FFFFFF",
                "minBorderMargin": null,
                "clickable": false,
                "hoverable": true,
                "autoHighlight": true,
                "mouseActiveRadius": 0.1
            },
            "yaxis": {

                "show": true,
                "position": "left",
                "mode": null,

                "color": "#F2F1EF",
                "tickColor": null,

                "font": null,
                "min": null,
                "max": null,
                "autoscaleMargin": 0.05,

                "transform": null,
                "inverseTransform": null,

                "ticks": null,
                "tickLength": 0,
                "tickDecimals": 0,
                "tickFormatter": null,
                "tickLength": null,

                "labelWidth": null,
                "labelHeight": null,
                "reserveSpace": null,

                "axisLabel": "Y Axis Label",
                "axisLabelUseCanvas": true,
                "axisLabelFontSizePixels": 14,
                "axisLabelFontFamily": "Arial",
                "axisLabelPadding": 5,

                "panRange": null,
                "zoomRange": false
            },
            "xaxis": {

                "show": true,
                "position": "bottom",
                "mode": null,

                "color": "#F2F1EF",
                "tickColor": null,

                "font": null,
                "min": null,
                "max": null,
                "autoscaleMargin": 0.1,

                "transform": null,
                "inverseTransform": null,

                "ticks": null,
                "tickLength": 0,
                "tickDecimals": null,
                "tickFormatter": null,
                "tickLength": null,

                "labelWidth": null,
                "labelHeight": null,
                "reserveSpace": null,

                "axisLabel": "X Axis Label",
                "axisLabelUseCanvas": true,
                "axisLabelFontSizePixels": 14,
                "axisLabelFontFamily": "Arial",
                "axisLabelPadding": 5,

                "panRange": null,

                "rotateTicks": 0
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
        }
    }
}());
