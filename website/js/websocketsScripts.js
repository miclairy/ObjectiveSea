/**
 * Created by cba62 on 21/09/17.
 */


var mySocket = new WebSocket("ws://132.181.14.60:2828");

mySocket.onerror = function (event) {
  console.log(event);
}

mySocket.onopen = function () {
  console.log("baa baa black sheep");

    let header = [];
    header = addIntToByteArray(header, 0, 0x47, 1);
    header = addIntToByteArray(header, 1, 0x83, 1);
    header = addIntToByteArray(header, 2, 55, 1);
    // header.push(135462345);
    // header.push(101);
    // header.push(4);
    alert(header);
    mySocket.send(header);
};

mySocket.onmessage = function() {
  alert("onmeassage");
}

mySocket.onclose = function () {
     alert("onclose");
}

function addIntToByteArray(array, start, item, numBytes) {
    for (let i = 0; i < numBytes; i ++) {
        array[start + i] = (byte) (item >> i * 8);

      }
    return array
}
