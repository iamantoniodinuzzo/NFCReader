package com.indisparte.hce.util

import android.nfc.NdefMessage
import java.math.BigInteger

/**
 * @author Antonio Di Nuzzo
 */
object Constants {
     val HEX_CHARS = "0123456789ABCDEF".toCharArray()

     //Commands
     // Response to the "Select Application" command sent by a terminal.
      val APDU_SELECT = byteArrayOf(
          0x00.toByte(), // CLA	- Class - Class of instruction
          0xA4.toByte(), // INS	- Instruction - Instruction code
          0x04.toByte(), // P1	- Parameter 1 - Instruction parameter 1
          0x00.toByte(), // P2	- Parameter 2 - Instruction parameter 2
          0x07.toByte(), // Lc field	- Number of bytes present in the data field of the command
          0xD2.toByte(),
          0x76.toByte(),
          0x00.toByte(),
          0x00.toByte(),
          0x85.toByte(),
          0x01.toByte(),
          0x01.toByte(), // NDEF Tag Application name
          0x00.toByte(), // Le field	- Maximum number of bytes expected in the data field of the response to the command
     )

     // Response to the "Capability Container Select" command.
      val CAPABILITY_CONTAINER_OK = byteArrayOf(
          0x00.toByte(), // CLA	- Class - Class of instruction
          0xa4.toByte(), // INS	- Instruction - Instruction code
          0x00.toByte(), // P1	- Parameter 1 - Instruction parameter 1
          0x0c.toByte(), // P2	- Parameter 2 - Instruction parameter 2
          0x02.toByte(), // Lc field	- Number of bytes present in the data field of the command
          0xe1.toByte(),
          0x03.toByte(), // file identifier of the CC file
     )

     // Response to the "Read Binary" command for the Capability Container.
      val READ_CAPABILITY_CONTAINER = byteArrayOf(
          0x00.toByte(), // CLA	- Class - Class of instruction
          0xb0.toByte(), // INS	- Instruction - Instruction code
          0x00.toByte(), // P1	- Parameter 1 - Instruction parameter 1
          0x00.toByte(), // P2	- Parameter 2 - Instruction parameter 2
          0x0f.toByte(), // Lc field	- Number of bytes present in the data field of the command
     )

     // In the scenario that we have done a CC read, the same byte[] match
     // for ReadBinary would trigger and we don't want that in succession
      var READ_CAPABILITY_CONTAINER_CHECK = false

      val READ_CAPABILITY_CONTAINER_RESPONSE = byteArrayOf(
          0x00.toByte(), 0x11.toByte(), // CCLEN length of the CC file
          0x20.toByte(), // Mapping Version 2.0
          0xFF.toByte(), 0xFF.toByte(), // MLe maximum
          0xFF.toByte(), 0xFF.toByte(), // MLc maximum
          0x04.toByte(), // T field of the NDEF File Control TLV
          0x06.toByte(), // L field of the NDEF File Control TLV
          0xE1.toByte(), 0x04.toByte(), // File Identifier of NDEF file
          0xFF.toByte(), 0xFE.toByte(), // Maximum NDEF file size of 65534 bytes
          0x00.toByte(), // Read access without any security
          0xFF.toByte(), // Write access without any security
          0x90.toByte(), 0x00.toByte(), // A_OKAY
     )

     // Response to the "NDEF Select" command.
      val NDEF_SELECT_OK = byteArrayOf(
          0x00.toByte(), // CLA	- Class - Class of instruction
          0xa4.toByte(), // Instruction byte (INS) for Select command
          0x00.toByte(), // Parameter byte (P1), select by identifier
          0x0c.toByte(), // Parameter byte (P1), select by identifier
          0x02.toByte(), // Lc field	- Number of bytes present in the data field of the command
          0xE1.toByte(),
          0x04.toByte(), // file identifier of the NDEF file retrieved from the CC file
     )

      val NDEF_READ_BINARY = byteArrayOf(
          0x00.toByte(), // Class byte (CLA)
          0xb0.toByte(), // Instruction byte (INS) for ReadBinary command
     )

     // Response to the "Read Binary" command for the NLEN field in the NDEF file.
      val NDEF_READ_BINARY_NLEN = byteArrayOf(
          0x00.toByte(), // Class byte (CLA)
          0xb0.toByte(), // Instruction byte (INS) for ReadBinary command
          0x00.toByte(),
          0x00.toByte(), // Parameter byte (P1, P2), offset inside the CC file
          0x02.toByte(), // Le field
     )

     // Response indicating success.
      val A_OKAY = byteArrayOf(
          0x90.toByte(), // SW1	Status byte 1 - Command processing status
          0x00.toByte(), // SW2	Status byte 2 - Command processing qualifier
     )

     // Response indicating an error.
      val A_ERROR = byteArrayOf(
          0x6A.toByte(), // SW1	Status byte 1 - Command processing status
          0x82.toByte(), // SW2	Status byte 2 - Command processing qualifier
     )

     // Identifier for the NDEF file.
      val NDEF_ID = byteArrayOf(0xE1.toByte(), 0x04.toByte())

     // NDEF message containing default text.
      var NDEF_URI = NdefMessage(Util.createTextRecord("en", "Default message", NDEF_ID))
      var NDEF_URI_BYTES: ByteArray = NDEF_URI.toByteArray()
      var NDEF_URI_LEN = Util.fillByteArrayToFixedDimension(
          BigInteger.valueOf(NDEF_URI_BYTES.size.toLong()).toByteArray(),
          2,
     )
}