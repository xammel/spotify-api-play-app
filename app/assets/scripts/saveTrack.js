    $(document).ready(function(){
      $("button").click(function(){
        var $button = $( this )
        let buttonValue = $button.attr("value")
        let buttonId = $button.attr("id")
        console.log(buttonValue);
        console.log(buttonId);

        //TODO finish testing this if logic - only post if save-track button is pressed
        if(buttonId === "save-track") {$.post(`/save?trackId=${buttonValue}`} ,
        {},
        function(data,status){
          alert("Track saved to your library");
        });
      });
    });