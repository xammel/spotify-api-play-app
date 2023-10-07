$(document).ready(function() {
    $("button").click(function() {
        var $button = $(this)
        let buttonValue = $button.attr("value")
        let buttonId = $button.attr("id")

        if (buttonId === "save-track") {
            $.post(`/save?trackId=${buttonValue}`, {},
                function(data, status) {
                    alert("Track saved to your library");
                });

            if ($button.find('i').attr("class").includes("fa-heart-o")) {
                $button.find('i').toggleClass('fa-heart-o fa-heart')
            }

        };
    });
});