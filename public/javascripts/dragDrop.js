$( function() {

    // There's the candidateList and the trash
    var $candidateList = $( "#candidateList" ),
      $ballot = $( "#ballot" );

    // Let the candidateList items be draggable
    $( "li", $candidateList ).draggable({
      cancel: "a.ui-icon", // clicking an icon won't initiate dragging
      revert: "invalid", // when not dropped, the item will revert back to its initial position
      containment: "document",
      helper: "clone",
      cursor: "move"
    });

    // Let the trash be droppable, accepting the candidateList items
    $ballot.droppable({
      accept: "#candidateList > li",
      classes: {
        "ui-droppable-active": "ui-state-highlight"
      },
      drop: function( event, ui ) {
        deleteImage( ui.draggable );
      }
    });

    // Let the candidateList be droppable as well, accepting items from the trash
    $candidateList.droppable({
      accept: "#ballot li",
      classes: {
        "ui-droppable-active": "custom-state-active"
      },
      drop: function( event, ui ) {
        recycleImage( ui.draggable );
      }
    });

    // Image deletion function
    var recycle_icon = "<a href='link/to/recycle/script/when/we/have/js/off' title='Recycle this image' class='ui-icon ui-icon-refresh'>Recycle image</a>";
    function deleteImage( $item ) {
      $item.fadeOut(function() {
        var $list = $( "ul", $ballot ).length ?
          $( "ul", $ballot ) :
          $( "<ul class='candidateList ui-helper-reset'/>" ).appendTo( $ballot );

        $item.find( "a.ui-icon-ballot" ).remove();
        $item.append( ).appendTo( $list ).fadeIn();
      });
    }

    // Image recycle function
    var trash_icon = "<a href='link/to/trash/script/when/we/have/js/off' title='Delete this image' class='ui-icon ui-icon-trash'>Delete image</a>";
    function recycleImage( $item ) {
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
    }
  } );
