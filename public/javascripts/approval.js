$(function() {
  $("#candidateDiv").on("click", '[id^="AAA_"].add', function() {
      $("#ballotDiv").append('<div class ="r0_1 centered" id ="' + this.id + '"><p>' + this.value + '</p><div>');
      $(this).replaceWith('<button id="' + this.id + '" type="button" class="btn btn-danger btn-sm remove vote" value= "' + this.value + ' ">remove</button>');
      return false;
    }),
    $("#candidateDiv").on("click", "button.remove", function() {
      $("#ballotDiv").children().remove("#" + this);
      $(this).replaceWith('<button id="' + this.id + '" type="button" class="btn btn-default btn-sm add vote" value= "' + this.value + ' ">Add</button>');
    })
})