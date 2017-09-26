var insults = ["probably never play this game again.", "No one has ever sailed as badly as you just did.", "you suck.", "rookie.",
    "better luck next time. Not that any of your friends will let you sail with them again.", "you died.", "Shutting down device"]

/**
 * Upon button press, sends a request game packet and changes the screen to control screen.
 */
function submitButtonPressed(){

    requestGame($("#codeBox").val());
    if(true){
        initControls("Emirates Team New Zealand", "skyblue");
        loadControls();
    }

}

function updateStats(speed, placing, totalCompetitors, health){
    console.log("updating");
    $("#boatSpeed").html(speed+"kn");
    $("#placing").html(placing + " / " + totalCompetitors);
    $("#boatHealth").html(health+"%");
}

function initControls(teamName, color){
    $("#boatNameText").html(teamName);
    changeColor(color);
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

