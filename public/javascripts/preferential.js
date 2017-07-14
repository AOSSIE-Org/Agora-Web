$( function() {
    $( "#sortable" ).sortable();
    $( "#sortable" ).disableSelection();
    $("#candidateDiv").on("click", '[id^="AAA_"].add',function() {
       $(".ballotDiv").append( '<li class="ui-state-default preferential borderless" id ="'+ this.id +'">'+ this.value +'</li>' );
       $( this ).replaceWith( '<button id="' + this.id + '" type="button" class="btn btn-danger btn-sm remove vote" value= "'+this.value +' ">remove</button>');
       return false;
   }),
  $("#candidateDiv").on("click" , "button.remove" , function () {
          $(".ballotDiv").children().remove("#"+this.id);
          $( this ).replaceWith('<button id="' + this.id+ '" type="button" class="btn btn-default btn-sm add vote" value= "'+this.value +' ">Add</button>');
    })

      $(".text-center.bottomm10").on("click", '#enter-vote-btn',function() {
        var str = "";
          $(".ballotDiv").children().each(function () {
                  str+=this.innerHTML+">";
                  str = str.replace(/\s/g, '');
                });
          $("#ballotinput").attr("value", str.substring(0, str.length-1));

      })

  } );
