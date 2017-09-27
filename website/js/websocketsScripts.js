/**
 * Created by cba62 on 21/09/17.
 */

let mySocket = null;
let serverSocket = null;
let myId = null;

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
    REGISTRATION_REQUEST:  {type:55, length:4},
    REGISTRATION_RESPONSE: {type:56, length:5},
    HOST_GAME_MESSAGE: {type:108, length:14}
}

let MESSAGE_FIELD = {
    GAME_CODE: {index:0, length:2},
    REGISTRATION_SOURCE_ID: {index:0, length:4},
    REGISTRATION_STATUS: {index:4, length:1},
    HOST_GAME_IP: {index:0, length:4},
    HOST_GAME_PORT: {index:4, length:4},
    HOST_GAME_IS_PARTY_MODE: {index:13, length:1}
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

    mySocket.onmessage = function (event) {
        decodePacket(new Uint8Array(event.data));
    }
}

function sendRegistrationPacket() {
    let header = createHeader(MESSAGE_TYPE.REGISTRATION_REQUEST.type, MESSAGE_TYPE.REGISTRATION_REQUEST.length);
    let body = new Uint8Array(MESSAGE_TYPE.REGISTRATION_REQUEST.length);
    addIntIntoByteArray(body, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.index, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.length, 1);
    let crc = createCrc(header, body);
    let packet = concatUint8ByteArrays(concatUint8ByteArrays(header, body), crc);
    let byteArray = new Uint8Array(packet);
    console.log(packet);
    console.log(byteArray.buffer);
    serverSocket.send(byteArray.buffer);
    // Currently sends this packet to the gameServer and we can see the boat appear on the game. However both the game and this app
    // crashes shortly after this happens.
}
/**
 * Creates a WebSocket connection to the Game Recorder Server
 */
function createGameServerSocket(/*String*/ip, port) {
    serverSocket = new WebSocket("ws://" + ip + ":" + port); // 2827 is the port game server runs on
    serverSocket.binaryType = 'arraybuffer';

    serverSocket.onerror = function (event) {
        console.log(event);
    }

    serverSocket.onopen = function () {
        console.log("Game Server connection established.");
        sendRegistrationPacket();
    };

    serverSocket.onclose = function () {
        alert("No connection to GameServer");
    }

    serverSocket.onmessage = function (event) {
        console.log("message from server");
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
    let packet = concatUint8ByteArrays(concatUint8ByteArrays(header, body), crc);
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
    let both = concatUint8ByteArrays(header, body);
    let crc = parseInt(crc32(both), 16);
    let array = [0, 0, 0, 0];
    addIntIntoByteArray(array, 0, crcLength, crc);
    return array;
}

function decodeRegistrationResponse(body) {
    let statusByte = body[MESSAGE_FIELD.REGISTRATION_STATUS.index];
    if (statusByte === 1) {
        myId = byteArrayRangeToInt(body, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.index, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.length);
        console.log("My Id: " + myId);
    } else {
        console.log("Registration failed.");
    }
}

function decodeHostGameMessage(body) {
    let longIp = ToUint32(byteArrayRangeToInt(body, MESSAGE_FIELD.HOST_GAME_IP.index, MESSAGE_FIELD.HOST_GAME_PORT.length));
    let port = byteArrayRangeToInt(body, MESSAGE_FIELD.HOST_GAME_PORT.index, MESSAGE_FIELD.HOST_GAME_PORT.length);
    let isPartyMode = body[MESSAGE_FIELD.HOST_GAME_IS_PARTY_MODE.index];
    let ip = ipLongToString(longIp);
    console.log("Ip: " + ip + " Port: " + port);
    console.log("IsPartyMode: " + isPartyMode);
    if (isPartyMode === 1) {
        createGameServerSocket(ip, port);
    }
}

decodePacket = function(packet) {
    let header = packet.subarray(0, HEADER_LENGTH);
    let bodyLength = byteArrayRangeToInt(packet, HEADER_FIELDS.MESSAGE_LENGTH.index, HEADER_FIELDS.MESSAGE_LENGTH.length);
    let bodyEnd = HEADER_LENGTH + bodyLength;
    let body = packet.subarray(HEADER_LENGTH, bodyEnd);
    let crc = packet.subarray(bodyEnd);
    let messageType = byteArrayRangeToInt(header, HEADER_FIELDS.MESSAGE_TYPE.index, HEADER_FIELDS.MESSAGE_TYPE.length);
    if (checkCRC(header, body, crc)){
        switch (messageType) {
            case MESSAGE_TYPE.HOST_GAME_MESSAGE.type:
                console.log("Hosted game message");
                decodeHostGameMessage(body);
                break;
            case MESSAGE_TYPE.REGISTRATION_RESPONSE.type:
                console.log("Client registration");
                decodeRegistrationResponse(body);
                break;
            case 120: //WebClientInit
                console.log("Web client init");
                break;
            case 121: //WebClientUpdate
                console.log("Web client update");
                break;
            default:
                console.log("Message type: " + messageType);
        }
    } else {
        console.error("CRC check failed")
    }
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
