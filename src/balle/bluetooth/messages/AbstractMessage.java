package balle.bluetooth.messages;

public abstract class AbstractMessage {

    // Can have 4 opcodes only
    public final static int    BITS_FOR_OPCODE  = 2;
    public final static int    BITS_PER_INT     = 32;
    public final static int    MAX_OPCODE_VALUE = (int) Math.pow(2, BITS_FOR_OPCODE) - 1;

    public final static int    OPCODE           = -1;
    public final static String NAME             = "";

    public abstract int getOpcode();

    public abstract String getName();

    public abstract int hash() throws InvalidOpcodeException;

    /**
	 * Function to hash the opcode of the message into correct place. Basically,
	 * it checks if the operation code is within the permitted range, then it
	 * returns a bitcode sequence of <BITS_PER_INT> bits with the opcode as the
	 * most significant bits
	 * 
	 * @return
	 * @throws InvalidOpcodeException
	 */
    protected int hashOpcode() throws InvalidOpcodeException {
        int opcode = getOpcode();
        if (opcode < 0)
            throw new InvalidOpcodeException(
                    "Opcode < 0 given. Opcode is supposed to be unsigned int.");

        if (opcode > AbstractMessage.MAX_OPCODE_VALUE)
            throw new InvalidOpcodeException("Opcode " + opcode + " cannot be fit into "
                    + AbstractMessage.BITS_PER_INT + " bits");

        return opcode << (AbstractMessage.BITS_PER_INT - AbstractMessage.BITS_FOR_OPCODE);
    }

	public static final int extractOpcodeFromEncodedMessage(int message) {
        return message >>> (BITS_PER_INT - BITS_FOR_OPCODE);
    }

	/**
	 * Given a message, this method splits it up into four bytes for
	 * transmission through the BufferStream
	 * 
	 * @param message
	 * @return
	 */
	public static final byte[] convertIntToFourBytes(int message) {
		// extract each byte from the full message
		byte byte1 = (byte) ((message & 0xFF000000) >> 24);
		byte byte2 = (byte) ((message & 0x00FF0000) >> 16);
		byte byte3 = (byte) ((message & 0x0000FF00) >> 8);
		byte byte4 = (byte) (message & 0x000000FF);

		// Return array of bites, most significant is first element
		byte[] bytes = { byte1, byte2, byte3, byte4 };
		return bytes;

	}

	/**
	 * Given an array of bytes this method reconstructs the full message for
	 * decoding by the NXT
	 * 
	 * @param bytes
	 * @return
	 */
	public static final int convertFourBytesToInt(byte[] bytes) {
		// Place each byte into its correct position in the 32 bit
		// sequence
		int byte1 = bytes[0] << 24;
		int byte2 = bytes[1] << 16;
		int byte3 = bytes[2] << 8;

		return (byte1 | byte2 | byte3 | bytes[3]);
	}
}
