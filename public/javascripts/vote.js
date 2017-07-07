$(function() {
  $("#1").on("click", function() {
     $("#mydiv").append( '<ol class="ui-sortable"><li id="r0_0">A</li></ol>' );
     $(this).parents('.r0_0').remove();
      $(this).parents('.ul').append('<button type="button" class="btn btn-default btn-sm remove">Remove</button>');
     console.log("hello");
     return false;
 }),
 $("#2").on("click", function() {
    $("#mydiv").append( '<p id=\'p_2\'> 2. Candidate 2 </p>' );
    $("#2").attr("disabled",true);
    return false;
}),
$("#3").on("click", function() {
   $("#mydiv").append( '<p id=\'p_3\'> 3. Candidate 3 </p>' );
   $("#3").attr("disabled",true);
   return false;
}),
      $(".container").on("click" , ".r1_0" , function () {
          console.log('Hello');
      })
})
