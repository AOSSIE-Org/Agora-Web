$(function() {
  $("#1").on("click", function() {
     $("#mydiv").append( '<p id=\'p_1\'> 1. Candidate 1 </p>' );
     $(this).attr("disabled",true);
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
})
})
