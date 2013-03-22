package balle.bluetooth.messages;

public abstract class AbstractSingleArgMessage extends AbstractMessage {

	/*
	 * Class that deals with bitcode messages that are for operations requiring
	 * just one argument. Such operations are kick and stop.
	 */
    private final int        arg1;
    private static final int BITS_PER_ARGUMENT = AbstractMessage.BITS_PER_INT
                                                       - AbstractMessage.BITS_FOR_OPCODE;

    public AbstractSingleArgMessage(int arg1) throws InvalidArgumentException {
        this.arg1 = arg1;

		/*
		 * Validation code to ensure arguments are within range. That is, ensure
		 * that the value provided as the argument isn't too large to fit in the
		 * allocated bit space
		 */
        if (arg1 < 0) {
            throw new InvalidArgumentException("Provided argument " + arg1
                    + " is < 0. Arguments should be unsigned. "
                    + " If you need a negative valued one, consider offsetting it "
                    + " so it is always positive.");

        } else if (arg1 > (int) Math.pow(2, AbstractSingleArgMessage.BITS_PER_ARGUMENT) - 1) {
            throw new InvalidArgumentException("Provided argument " + arg1
                    + " exceeds number of bits per argument");
        }

    }

    protected int hashArguments() {
        // No shifting needed in this case
        return this.arg1;
    }

	/*
	 * Combine the operation code and arguments into one bitcode sequence for
	 * transmission.
	 * 
	 * @see balle.bluetooth.messages.AbstractMessage#hash()
	 */
    @Override
    public int hash() throws InvalidOpcodeException {
        return hashOpcode() | hashArguments();
    }

	/*
	 * Returns just the bits containing the arguments which are in this case the
	 * first 30 bits.
	 */
    public static int decodeArgumentsFromHash(int hash) {
        return hash & 0x3FFFFFFF;
    }

    public int getArgument() {
        return this.arg1;
    }

}
