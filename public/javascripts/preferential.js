$( function() {
    $( "#sortable" ).sortable();
    $( "#sortable" ).disableSelection();
    $("#candidateDiv").on("click", '[id^="AAA_"].add',function() {
    $(".ballotDiv").append( '<li class="ui-state-default preferential borderless" id ="'+ this.id +'">'+ '<button id= "' + this.id + '" type="button" class = "btn-danger remove" value= "'+this.value +' "><span class="glyphicon glyphicon-remove"></span></button>'+this.value +'</li>' );
    $(this).parent().remove();

       return false;
   }),
  $(".ballotDiv").on("click" , "button.remove" , function () {
          $(".ballotDiv").children().remove("#"+this.id);
          $("#candidateDiv").children("ul").append('<li class="r0_0"><button id='+ this.id +' value='+ this.value+ 'type="button" class="btn btn-default btn-sm add vote">Add</button><span>'+this.value+'</span></li>')
    })

      $(".text-center.bottomm10").on("click", '#enter-vote-btn',function() {
        var str = "";
          $(".ballotDiv").children().each(function () {
                  str+=this.children[0].value+">";
                  str = str.replace(/\s/g, '');
                });
          $("#ballotinput").attr("value", str.substring(0, str.length-1));

      })

  } );
