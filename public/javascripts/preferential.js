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


  } );
