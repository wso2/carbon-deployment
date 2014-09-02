var render = function (theme, data, meta, require) {

    if(data.error.length == 0 ){
    theme('index', {
    config: [{
        context: {
            gadgetsUrlBase: data.config.gadgetsUrlBase
        }
    }],
    title: [{
        context:{
            page_title:'AS Dashboard'
        }
    }],
    header: [
        {
            partial: 'header',
            context:{
                user_name: 'dakshika@wso2.com ',
                user_avatar:'dakshika'
            }
        }
    ],
    'sub-header': [
            {
                partial: 'sub-header',
                context:{
                    appname : data.appname
                }
            }
        ],
    left_side:[
              	{
                partial: 'left_side',
                context: {
                	user_name: 'dakshika@wso2.com ',
                	user_avatar:'dakshika',
                    breadcrumb:'Service Cluster System Statistics'
                }
            }
     ],
     right_side: [

            {
            	partial: 'aggregated-index',
            	context:{
            		data:  data.panels,
                    updateInterval: data.updateInterval
            	}
            }
     ]
    });

    }else{

        theme('index', {
        title: [
             
         ],
         header:[
                    {
                        partial: 'header_login'
                    }
         ],
         body: [

                {
                    partial: 'error',
                    context:{
                        error:  data.error
                    }
                }
         ]
        });
    }
};