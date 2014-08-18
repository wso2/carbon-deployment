var render = function (theme, data, meta, require) {

    if(data.error.length == 0 ){
    theme('index', {
    title: [{
        context:{
            page_title:'AS Dashboard'
        }
    }],
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
            	partial: 'right_side',
            	context:{
                    user_name: 'dakshika@wso2.com ',
                    user_avatar:'dakshika',
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
                    partial: 'header_login',
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