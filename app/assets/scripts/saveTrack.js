    $(document).ready(function(){
      $("button").click(function(){
        var $button = $( this )
        let buttonValue = $button.attr("value")
        console.log(buttonValue);
        $.post(`/save?trackId=${buttonValue}`,
        {},
        function(data,status){
          alert("Track saved to your library");
        });
      });
    });