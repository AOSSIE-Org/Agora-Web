$(function() {
  $("#candidateDiv").on("click", '[id^="AAA_"].add',function() {
     $("#ballotDiv").children().remove();
     $("#ballotDiv").append( '<div class ="r0_1 centered"><p>'+this.value+'</p><div>' );
     $("#ballot").attr("value",this.value);
     $('[id^="AAA_"]').attr("disabled",false);
     $(this).attr("disabled",true);
     return false;
 });
})
