function htmlEncode(str){
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace('\n','').replace('\r','');
}