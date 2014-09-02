$(document).ready(function() {
    var table = $('#main_app_table').dataTable( {
        "ajax" :  site_context + '/api/as-data.jag?action=apps'
    } );

    $('#main_app_table tbody').on('click', 'tr', function(){
        if($(this).hasClass('selected')){
            $(this).removeClass('selected');
        } else{
            var webapp = table.fnGetData(this)[0];
            table.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
            document.location.href = 'webapps/' + webapp + '/';
        }
    });
} );