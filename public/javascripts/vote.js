$(function() {
  $("#mydiv1").on("click", '[id^="AAA_"].add',function() {
     $("#mydiv").append( '<div class ="r0_1 centered"><p>'+this.value+'</p><div>' );
     $('[id^="AAA_"].add').attr("disabled",true);
     $( this ).replaceWith( '<button id="' + this.id + '" type="button" class="btn btn-danger btn-sm remove vote" value= "'+this.value +' ">remove</button>');
     return false;
 }),
$("#mydiv1").on("click" , "button.remove" , function () {
        $("#mydiv").children().remove();
        $('[id^="AAA_"]').attr("disabled",false);
        $( this ).replaceWith('<button id="' + this.id+ '" type="button" class="btn btn-default btn-sm add vote" value= "'+this.value +' ">Add</button>');
  })
})
