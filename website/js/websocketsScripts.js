/**
 * Created by cba62 on 21/09/17.
 */

let mySocket = null;
createGameRecorderSocket();

let AC35_SYNC_BYTE_1 = 71;
let AC35_SYNC_BYTE_2 = 131;
let HEADER_LENGTH = 15;

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
    let header = new Uint8Array(HEADER_LENGTH);
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
    mySocket = new WebSocket("ws://127.0.0.1:2827/connect"); // 2827 is the port game server runs on
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

    mySocket.onmessage = function (event) {
        decodePacket(new Uint8Array(event.data));
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
    let body = new Uint8Array(2);
    addIntIntoByteArray(body, MESSAGE_FIELD.GAME_CODE.index, MESSAGE_FIELD.GAME_CODE.length, code);
    let crc = createCrc(header, body);
    let packet = concat(concat(header, body), crc);
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
    let both = concat(header, body);
    let crc = parseInt(crc32(both), 16);
    let array = [0, 0, 0, 0];
    addIntIntoByteArray(array, 0, crcLength, crc);
    return array;
}

decodePacket = function(packet) {
    let header = packet.subarray(0, HEADER_LENGTH);
    let bodyLength = byteArrayrangeToInt(packet, HEADER_FIELDS.MESSAGE_LENGTH.index, HEADER_FIELDS.MESSAGE_LENGTH.length);
    let bodyEnd = HEADER_LENGTH + bodyLength;
    let body = packet.subarray(HEADER_LENGTH, bodyEnd);
    let crc = packet.subarray(bodyEnd);
    let messageType = byteArrayrangeToInt(header, HEADER_FIELDS.MESSAGE_TYPE.index, HEADER_FIELDS.MESSAGE_TYPE.length);
    if (checkCRC(header, body, crc)){
        switch (messageType) {
            case 56:
                console.log("client reg");

                break;
            case 120:
                console.log("Web client init");
                break;
            case 121:
                console.log("Web client update");
                break;
        }
    } else {
        console.error("CRC check failed")
    }
}

byteArrayrangeToInt = function (array, beginIndex, length) {
    let total = 0;
    if (length <= 0 || length > 4){
        console.error("The length of the range must be between 1 and 4 inclusive");
    }
    for (let i = (beginIndex + length) - 1; i >= beginIndex; i--){
        total = (total << 8) + (array[i] & 0xFF);
    }
    return total;
}

checkCRC = function (header, body, crc) {
    let expectedCRC = createCrc(header, body);
    for (let i = 0; i < 4; i++){
        if (expectedCRC[i] !== crc[i]){
            console.log(expectedCRC);
            console.log(crc);
            return false;
        }
    }
    return true;
}

concat = function (firstArray, secondArray) {
    let joinedArray = new Uint8Array(firstArray.length + secondArray.length);
    joinedArray.set(firstArray);
    joinedArray.set(secondArray, firstArray.length);
    return joinedArray;
}