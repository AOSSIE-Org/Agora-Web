$( function() {
    var $selected = []
    // There's the candidateList
    var $candidateList = $( "#candidateList" ),
      $ballot = $( "#ballotL" );

    // Let the candidateList items be draggable
    $( "li", $candidateList ).draggable({
      cancel: "a.ui-icon", // clicking an icon won't initiate dragging
      revert: "invalid", // when not dropped, the item will revert back to its initial position
      containment: "document",
      helper: "clone",
      cursor: "move"
    });

    // Let the ballot be droppable, accepting the candidateList items
    $ballot.droppable({
      accept: "#candidateList > li",
      classes: {
        "ui-droppable-active": "ui-state-highlight"
      },
      drop: function( event, ui ) {
        selectCandidate( ui.draggable );
      }
    });

    // Let the candidateList be droppable as well, accepting items from the ballot
    $candidateList.droppable({
      accept: "#ballot li",
      classes: {
        "ui-droppable-active": "custom-state-active"
      },
      drop: function( event, ui ) {
        removeCandidate( ui.draggable );
      }
    });


    function selectCandidate( $item ) {
      $item.fadeOut(function() {
        var $list = $( "ul", $ballot ).length ?
          $( "ul", $ballot ) :
          $( '<ul class="candidateList ui-helper-reset" id="ballot"/>' ).appendTo( $ballot );

        $item.find( "a.ui-icon-ballot" ).remove();
        $item.append( ).appendTo( $list ).fadeIn();

        // $selected.pop();
        $selected.push($item.children().text());
        // $selected.push("}");
        // console.log($selected);
        $("#ballotinput").attr("value",$selected.toString());
      });
    }





    function removeCandidate( $item ) {
      $item.fadeOut(function() {
        $item
          .find( "a.ui-icon-refresh" )
            .remove()
          .end()
          .css( "width", "215px")
          .append( )
          .appendTo( $candidateList )
          .fadeIn();
      });

      var index = $selected.indexOf($item.children().text());
      if (index > -1) {
        $selected.splice(index, 1);
      }
// console.log($selected);

      // console.log($($ballot).find("[id^='ballot'].candidateList.ui-helper-reset"));

      $("#ballotinput").attr("value",$selected.toString());
  }
})
