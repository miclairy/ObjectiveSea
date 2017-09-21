

// $(document).ready(function(){
//     $( "#codeEntryForm" ).on( "c", loadControls());
//
// });

var insults = ["probably never play this game again.", "No one has ever sailed as badly as you just did.", "you suck.", "rookie.",
    "better luck next time. Not that any of your friends will let you sail with them again.", "you died.", "Shutting down device"]

function submitButtonPressed(){

  //TODO: authenitifaction and game connection logic goes here

    if(true){
        initControls("Emerites Team New Zealand");
        changeColor("skyblue");
        loadControls();
    }

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
    $("#infoScreen").fadeIn(1000);
}

function changeColor(color){
    $("#controlsPage").css("background-color", color);
    $(".directionArrow").css("color", color);
    $("#infoScreen").css("background-color", color);


}


let socket = new WebSocket("ws://132.181.13.96:2828", "checking Life Works");

socket.onopen = function (event) {
  socket.sendBytes("hello");
};
