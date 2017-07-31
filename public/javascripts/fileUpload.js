$( function() {
var file = document.getElementById('emailFile');
file.onchange = function(e){
  console.log(file);
  try{
    var ext = this.value.match(/\.([^\.]+)$/)[1];

    switch(ext)
    {
        case 'txt':
            break;
        default:
            alert('not allowed');
            this.value='';
    }
  } catch(e){
      alert('not allowed');
      this.value='';
  }
}
})
