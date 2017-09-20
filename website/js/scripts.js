

// $(document).ready(function(){
//     $( "#codeEntryForm" ).on( "c", loadControls());
//
// });


function loadControls(){

  console.log("hello");
  $("#codeForm").toggle(2000);
    $("#controls").toggle(2000);


}


let socket = new WebSocket("ws://132.181.13.96:2828", "checkingLifeWorks");

socket.onopen = function (event) {
  socket.sendBytes("hello");
};
