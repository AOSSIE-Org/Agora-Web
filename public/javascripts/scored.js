var rangeSlider = function(){
  var slider = $('.range-slider'),
      range = $('.range-slider__range'),
      value = $('.range-slider__value');

  slider.each(function(){

    value.each(function(){
      var value = $(this).prev().attr('value');
      $(this).html(value);
    });

    range.on('input', function(){
      $(this).next(value).html(this.value);
      $(this).attr('value', this.value)
    });
  });

};
$( function() {

  $(".text-center.bottomm10").on("click", '#enter-vote-btn',function() {
    var str = "";
    console.log("hello");
    $(".col-md-12.scored.centered").find(".range-slider__range").each(function () {
          str+="("+this.name+":"+ this.value + ")";
          });
    $("#ballotinput").attr("value", str);

  })
  console.log("Hello");

}
)


rangeSlider();
