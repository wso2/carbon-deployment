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

var renderAll = function(){
    var opt, gadgetUrl;
    $('.gadget-holder').each(function (i, el) {
        var $el = $(el);
        if($el.is(':visible') && !Boolean($el.attr('data-rendered'))){
            gadgetUrl = config.gadgetsUrlBase + '/' + $el.attr('data-gadget') + '/' + $el.attr('data-gadget') + '.xml';
            opt = {
                prefs: {
                    dataSource: caramel.context + '/api/as-data.jag',
                    startTime: (state.start) ? moment(state.start, 'X').format('YYYY-MM-DD HH:mm') : undefined,
                    endTime: (state.end) ? moment(state.end, 'X').format('YYYY-MM-DD HH:mm') : undefined,
                    node: state.node || undefined,
                    appStatType: $el.attr('data-type'),
                    appname: appname || ''
                }
            };
            UESContainer.renderGadget($el.attr('id'), gadgetUrl, opt);
            $el.attr('data-rendered',true);
        }
    });
};

$(function(){
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        renderAll();
    });
    renderAll();
});
