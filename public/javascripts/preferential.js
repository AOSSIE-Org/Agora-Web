$( function() {
    $( "#sortable" ).sortable();
    $( "#sortable" ).disableSelection();
    $("#mydiv1").on("click", '[id^="AAA_"].add',function() {
       $(".mydiv").append( '<li class="ui-state-default preferential borderless" id ="'+ this.id +'">'+ this.value +'</li>' );
       $( this ).replaceWith( '<button id="' + this.id + '" type="button" class="btn btn-danger btn-sm remove vote" value= "'+this.value +' ">remove</button>');
       return false;
   }),
  $("#mydiv1").on("click" , "button.remove" , function () {
          $(".mydiv").children().remove("#"+this.id);
          $( this ).replaceWith('<button id="' + this.id+ '" type="button" class="btn btn-default btn-sm add vote" value= "'+this.value +' ">Add</button>');
    })


  } );
