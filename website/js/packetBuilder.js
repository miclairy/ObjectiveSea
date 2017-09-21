/**
 * Created by mjt169 on 18/07/17.
 * @class
 */
var PacketBuilder = (function () {
    function PacketBuilder() {
        /*private*/ this.HEADER_LENGTH = 15;
        /*private*/ this.sourceID = -1;
    }
    /**
     * Simplifier function for adding stream field to byte array
     * @param {Array} array array which to add the int
     * @param field the AC35StreamField field to add
     * @param {number} item the item to add
     * @param {number} fieldStart
     * @param {number} fieldLength
     */
    PacketBuilder.prototype.addFieldToByteArray = function (array, fieldStart, fieldLength, item) {
        this.addIntIntoByteArray(array, fieldStart, item, fieldLength);
    };
    /**
     * Splits an integer into a few bytes and adds it to a byte array
     * @param {Array} array array which to add the int
     * @param {number} start index it start adding
     * @param {number} item item to add
     * @param {number} numBytes number of bytes to split the int into
     */
    PacketBuilder.prototype.addIntIntoByteArray = function (array, start, item, numBytes) {
        for (let i = 0; i < numBytes; i++) {
            array[start + i] = ((item >> i * 8) | 0);
        };
    };
    /**
     * creates a header byte array which has 2 snyc bytes, a type, timestamp, source id which is an identifier for who
     * is sending the message
     * message length if it is not variable.
     * @param {number} type the integer type of the message
     * @return {Array} a byte array of the header
     * @param {number} messageLength
     */
    PacketBuilder.prototype.createHeader = function (type, messageLength) {
        var header = (function (s) { var a = []; while (s-- > 0)
            a.push(0); return a; })(this.HEADER_LENGTH);
        header[0] = (71 | 0);
        header[1] = (131 | 0);
        this.addFieldToByteArray(header, 2, 1, type);
        this.addFieldToByteArray(header, 3, 6, 0);
        this.addFieldToByteArray(header, 9, 4, this.sourceID);
        if (messageLength !== -1) {
            this.addFieldToByteArray(header, 2, 1, messageLength);
        }
        return header;
    };
    /**
     * Combines the header, body and crc byte arrays together
     * @param {Array} header the message header
     * @param {Array} body the message body
     * @param {Array} crc the computed crc to be sent
     * @return {Array} the combined message consisting of all three parts
     * @private
     */
    /*private*/ PacketBuilder.prototype.combineMessageParts = function (header, body, crc) {
        var combined = (function (s) { var a = []; while (s-- > 0)
            a.push(0); return a; })(header.length + body.length + crc.length);
        for (var i = 0; i < header.length; i++) {
            combined[i] = header[i];
        }
        ;
        for (var i = 0; i < body.length; i++) {
            combined[i + header.length] = body[i];
        }
        ;
        for (var i = 0; i < crc.length; i++) {
            combined[i + header.length + body.length] = crc[i];
        }
        ;
        return combined;
    };
    return PacketBuilder;
}());
PacketBuilder["__class"] = "PacketBuilder";