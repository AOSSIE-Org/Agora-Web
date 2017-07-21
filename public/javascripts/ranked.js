$( function() {

  $(".text-center.bottomm10").on("click", '#enter-vote-btn',function() {
    var str = "";
    $(".col-md-12.scored.centered").find(".form-control.input-sm").each(function () {
          str+="("+this.id+":"+ this.value + ")";
          });
    $("#ballotinput").attr("value", str);

  })

}
)
