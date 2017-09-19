let socket = new WebSocket("ws://132.181.13.96:2828", "checkingLifeWorks");

socket.onopen = function (event) {
  socket.sendBytes("hello");
};
