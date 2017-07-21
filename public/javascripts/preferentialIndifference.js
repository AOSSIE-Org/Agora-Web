

$( function() {
    $( "#sortable" ).sortable();
    $( "#sortable" ).disableSelection();
    $("#candidateDiv").on("click", '[id^="AAA_"].add',function() {
       $(".ballotDiv").append( '<li class="ui-state-default preferential borderless" id ="'+ this.id +'">'+ this.value +'</li>' );
       $( this ).replaceWith( '<button id="' + this.id + '" type="button" class="btn btn-danger btn-sm remove vote" value= "'+this.value +' ">remove</button>');
       return false;
   }),




  $("#candidateDiv").on("click" , "button.remove" , function () {
          // $(".ballotDiv").children().remove("#"+this.id);
          $(".sortable").children().remove("#"+this.id);
          $( this ).replaceWith('<button id="' + this.id+ '" type="button" class="btn btn-default btn-sm add vote" value= "'+this.value +' ">Add</button>');
    }),

      $(".text-center.bottomm10").on("click", '#enter-vote-btn',function() {
        var con = true;
        var i = 0;
        var str = "";
         while(con){
        var j =1;
        con = false;
         $('[id^="sortable"]').each(function () {
           var that = this.children
           if(that[i]!=undefined){
               if(j!=1){
                   str = str+ "=" +that[i].id;
               }
               else{
                  str = str + that[i].id
               }
               con=true;
           }
           j++;
       });
       str = str + ">";
       i++;
     }
    $("#ballotinput").attr("value", str.substring(0, str.length-2).replace(/AAA_/g, ""));

  } );
});

  $( function() {
    $( "ul.droptrue" ).sortable({
      connectWith: "ul"
    });

    $( "ul.dropfalse" ).sortable({
      connectWith: "ul",
      dropOnEmpty: false
    });

    $( "#sortable1, #sortable2, #sortable3" ).disableSelection();
  } );
