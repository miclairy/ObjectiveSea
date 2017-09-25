var insults = ["probably never play this game again.", "No one has ever sailed as badly as you just did.", "you suck.", "rookie.",
    "better luck next time. Not that any of your friends will let you sail with them again.", "you died.", "Shutting down device"]

let BOAT_ID = 1;


/**
 * Upon button press, sends a request game packet and changes the screen to control screen.
 */
function submitButtonPressed(){



    requestGame($("#codeBox").val());
    if(true){
        initControls("Emerites Team New Zealand");
        changeColor("skyblue");
        loadControls();
        initButtonListeners();

    }
}

function initButtonListeners(){
    var timeout;
    $(".boatActionPress, .boatActionHold").click(function (event) {
        let id = $("#"+event.target.id).attr('name');
        console.log(id);

    })
    $(".boatActionHold").mousedown(function (event) {
        timeout = setInterval(function(){
            let id = $("#"+event.target.id).attr('name');
            console.log(id);
        }, 100);
    })
    $(".boatActionHold").mouseup(function(){
        clearInterval(timeout);
        return false;
    });
}


function updateStats(speed, placing, health){
    console.log("updating");
    $("#boatSpeed").html(speed+"kn");
    $("#placing").html(placing);
    $("#boatHealth").html(health+"%");
}

function initControls(teamName){
    $("#boatNameText").html(teamName);
    $("#boatSpeed").html("0kn");
    $("#placing").html("-");
    $("#boatHealth").html("100%");
}

function loadControls(){
  $("#codeForm").fadeOut(1000);
    $("#controls").fadeIn(1000);
}

function loadInfoScreen(){
    var rand = insults[Math.floor(Math.random() * insults.length)];
    $("#infoScreenText").html(rand);
    $("#controls").fadeOut(1000);
    $("#infoScreen").fadeIn(1000);
}

function changeColor(color){
    $("#controlsPage").css("background-color", color);
    $(".directionArrow").css("color", color);
    $("#infoScreen").css("background-color", color);
    $("body").css("background-color", color);
}
