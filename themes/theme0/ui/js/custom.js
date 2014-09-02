jQuery(window).load(function() {
   jQuery('#loader').fadeOut();
   jQuery('#contentloader').delay(350).fadeOut(function(){
      jQuery('as_body').delay(350).css({'overflow':'visible'});
   });
});

jQuery(document).ready(function() {
   
   jQuery('.toggle-sidebar').tooltip('hide');

   //My toggle menu
   jQuery('.toggle-sidebar').click(function(){
     
      var as_body = jQuery('body');
      
      if(as_body.css('position') != 'relative') {
         
         if(!as_body.hasClass('leftpanel-collapsed')) {
            as_body.addClass('leftpanel-collapsed');
            jQuery('.nav-as ul').attr('style','');
            
            jQuery(this).addClass('menu-collapsed');
            
         } else {
            as_body.removeClass('leftpanel-collapsed chat-view');
            jQuery('.nav-as li.active ul').css({display: 'block'});
            
            jQuery(this).removeClass('menu-collapsed');
            
         }
      } else {
         
         if(as_body.hasClass('leftpanel-show'))
            as_body.removeClass('leftpanel-show');
         else
            as_body.addClass('leftpanel-show');
         
         panelfixdevice();         
      }

   });

   function panelfixdevice() {
      if(jQuery(document).height() > jQuery('.right-side').height()){
         jQuery('.right-side').height(jQuery(document).height());
      }
   }
   panelfixdevice();


   // Toggle Left Menu
   jQuery('.left-content-list .nav-parent > a').on('click', function() {

      var parent = jQuery(this).parent();
      var sub = parent.find('> ul');

      if(!jQuery('as_body').hasClass('leftpanel-collapsed')) {
         if(sub.is(':visible')) {
            sub.slideUp(200, function(){
               parent.removeClass('nav-active');
               jQuery('.right-side').css({height: ''});
               panelfixdevice();
            });
         } else {
            hideSMenu();
            parent.addClass('nav-active');
            sub.slideDown(200, function(){
               panelfixdevice();
            });
         }
      }
      return false;
   });
   

   function hideSMenu() {
      jQuery('.left-content-list .nav-parent').each(function() {
         var t = jQuery(this);
         if(t.hasClass('nav-active')) {
            t.find('> ul').slideUp(200, function(){
               t.removeClass('nav-active');
            });
         }
      });
   }



   jQuery('.nav-as > li').hover(function(){
      jQuery(this).addClass('nav-hover');
   }, function(){
      jQuery(this).removeClass('nav-hover');
   });
   
});