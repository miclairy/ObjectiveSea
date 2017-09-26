var insults = ["probably never play this game again.", "No one has ever sailed as badly as you just did.", "you suck.", "rookie.",
    "better luck next time. Not that any of your friends will let you sail with them again.", "you died.", "Shutting down device"]

let BOAT_ID = 1;
let BOAT_ACTION = {
    VMG: {value:1},
    SAILS : {value:2},
    TACK_GYBE : {value:4},
    UP_WIND : {value:5},
    DOWN_WIND : {value:6},
}

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

/**
 * Initialises boat control buttons on website
 * Upwind, downwind have actions for holding down
 */
function initButtonListeners(){
    var timeout;
    $(".boatActionPress, .boatActionHold").click(function (event) {
        let name = $("#"+event.target.id).attr('name');
        createBoatActionMessage(name);
    })
    $(".boatActionHold").mousedown(function (event) {
        timeout = setInterval(function(){
            let name = $("#"+event.target.id).attr('name');
            createBoatActionMessage(name);
        }, 100);
    })
    $(".boatActionHold").mouseup(function(){
        clearInterval(timeout);
        return false;
    });
    $('.boatActionHold').mouseout(function () {
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

/**
 * Checks code of button pressed, sends corresponding boat action message
 * @param name
 */
function createBoatActionMessage(name){
    switch(name){
        case "vmg":
            sendBoatActionMessage(BOAT_ACTION.VMG.value, BOAT_ID);
            break;
        case "sails":
            sendBoatActionMessage(BOAT_ACTION.SAILS.value, BOAT_ID);
            break;
        case "tackGybe":
            sendBoatActionMessage(BOAT_ACTION.TACK_GYBE.value, BOAT_ID);
            break;
        case "upwind":
            sendBoatActionMessage(BOAT_ACTION.UP_WIND.value, BOAT_ID);
            break;
        case "downwind":
            sendBoatActionMessage(BOAT_ACTION.DOWN_WIND.value, BOAT_ID);
            break;
        default:
            console.log("Unknown Button Pressed");
            break;
    }
};
