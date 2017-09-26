/**
 * Created by cba62 on 21/09/17.
 */

let mySocket = null;
createGameRecorderSocket();

let AC35_SYNC_BYTE_1 = 71;
let AC35_SYNC_BYTE_2 = 131;

let HEADER_FIELDS = {
    MESSAGE_TYPE: {index: 2, length: 1},
    MESSAGE_LENGTH : {index: 13, length:2},
}

let MESSAGE_TYPE = {
    GAME_REQUEST: {type:114, length:2},
}

let MESSAGE_FIELD = {
    GAME_CODE: {index:0, length:2}
}

/**
 * Creates the header for the AC35 protocol messages
 * @param type The type of the message
 * @param messageLength The length of the payload
 */
createHeader = function (type, messageLength) {
    let HEADER_LENGTH = 15;
    let header = (function (s) {
        let a = [];
        while (s-- > 0)
            a.push(0);
        return a;
    })(HEADER_LENGTH);
    header[0] = AC35_SYNC_BYTE_1;
    header[1] = AC35_SYNC_BYTE_2;
    this.addIntIntoByteArray(header, HEADER_FIELDS.MESSAGE_TYPE.index, HEADER_FIELDS.MESSAGE_TYPE.length, type);
    this.addIntIntoByteArray(header, HEADER_FIELDS.MESSAGE_LENGTH.index, HEADER_FIELDS.MESSAGE_TYPE.length, messageLength);
    return header;
}

/**
 * Puts an int into its bytes and put them into a byte array
 * @param array The array to be put into
 * @param start The starting index to put number into
 * @param numBytes The number of bytes of the int
 * @param item The actual value of the int
 */
addIntIntoByteArray = function (array, start, numBytes, item) {
    for (let i = 0; i < numBytes; i++) {
        array[start + i] = (item >> (i * 8)) & (0xFF);
    }
};

/**
 * Creates a WebSocket connection to the Game Recorder Server
 */
function createGameRecorderSocket() {
    mySocket = new WebSocket("ws://127.0.0.1:2827"); // 2827 is the port game server runs on
    mySocket.binaryType = 'arraybuffer';

    mySocket.onerror = function (event) {
        console.log(event);
    }

    mySocket.onopen = function () {
        console.log("WebSocket connection established.");
    };

    mySocket.onclose = function () {
        alert("No connection to GameRecorder");
    }
}

/**
 * Sends a request game packet to the Game Recorder
 * @param code The room code entered
 */
requestGame = function(code) {
    if(mySocket == null){
        createGameRecorderSocket();
    }
    let header = createHeader(MESSAGE_TYPE.GAME_REQUEST.type, MESSAGE_TYPE.GAME_REQUEST.length);
    let body = [0, 0];
    addIntIntoByteArray(body, MESSAGE_FIELD.GAME_CODE.index, MESSAGE_FIELD.GAME_CODE.length, code);
    let crc = createCrc(header, body);
    let packet = header.concat(body).concat(crc);
    let byteArray = new Uint8Array(packet);
    mySocket.send(byteArray.buffer);
}

/**
 * Calculates and return the CRC over the header and body
 * @param header The header of the packet
 * @param body The body of the packet
 * @returns {[number,number,number,number]} The CRC of the packet
 */
createCrc = function(header, body){
    let crcLength = 4;
    let both = header.concat(body);
    let crc = parseInt(crc32(both), 16);
    let array = [0, 0, 0, 0];
    addIntIntoByteArray(array, 0, crcLength, crc);
    return array;
}
