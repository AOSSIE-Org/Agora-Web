$(document).ready(function() {

  var navListItems = $('div.setup-panel div a'),
    allWells = $('.setup-content'),
    allNextBtn = $('.nextBtn');

  allWells.hide();

  navListItems.click(function(e) {
    e.preventDefault();
    var $target = $($(this).attr('href')),
      $item = $(this);

    if (!$item.hasClass('disabled')) {
      navListItems.removeClass('btn-primary').addClass('btn-default');
      $item.addClass('btn-primary');
      allWells.hide();
      $target.show();
      $target.find('input:eq(0)').focus();
    }
  });

  allNextBtn.click(function() {
    var curStep = $(this).closest(".setup-content"),
      curStepBtn = curStep.attr("id"),
      nextStepWizard = $('div.setup-panel div a[href="#' + curStepBtn + '"]').parent().next().children("a")
    curInputs = curStep.find("input[type='text'],input[type='url']"),
      isValid = true;

    $(".form-group").removeClass("has-error");
    for (var i = 0; i < curInputs.length; i++) {
      if (!curInputs[i].validity.valid) {
        isValid = false;
        $(curInputs[i]).closest(".form-group").addClass("has-error");
      }
    }

    if (isValid)
      nextStepWizard.removeAttr('disabled').trigger('click');
  });

  $('div.setup-panel div a.btn-primary').trigger('click');

  var date_input = $('input[name="start"]'); //our start date input has the name "start"
  var container = $('.bootstrap-iso form').length > 0 ? $('.bootstrap-iso form').parent() : "body";
  var options = {
    container: container,
    todayHighlight: true,
    autoclose: true,
    startDate: new Date()
  };
  date_input.datepicker(options);



  var date_inputend = $('input[name="end"]'); //our end date input has the name "end"
  var containerend = $('.bootstrap-iso form').length > 0 ? $('.bootstrap-iso form').parent() : "body";
  var optionsend = {
     container: containerend,
     todayHighlight: true,
     autoclose: true,
     startDate: new Date()
   };
  date_inputend.datepicker(optionsend);

  date_input[0].onchange = function(e){
    var date = new Date(this.value)
    date.setDate(date.getDate() + 1)
    date_inputend.datepicker('setStartDate', date)
  }

  $("a.my-tool-tip").tooltip();

});
