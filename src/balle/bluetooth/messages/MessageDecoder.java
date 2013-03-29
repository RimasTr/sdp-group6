package balle.bluetooth.messages;

/**
 * Factory for Messages. Decodes the message from opcode.
 */
public class MessageDecoder {

    /**
	 * Decodes a hashed message. First it extracts and checks the the operation
	 * code of the message to see which operation it is trying to execute. Then
	 * it extracts the arguments, returning an instance of the corrosponding
	 * message class.
	 * 
	 * @param hashedMessage
	 *            the hashed message
	 * @return An instance of particular message
	 * @throws InvalidArgumentException
	 *             when something went horribly wrong
	 */
    public AbstractMessage decodeMessage(int hashedMessage)
            throws InvalidArgumentException {
		switch (AbstractMessage.extractOpcodeFromEncodedMessage(hashedMessage)) {
            case MessageKick.OPCODE:
                int isPenalty = MessageKick
                        .decodeArgumentsFromHash(hashedMessage);
                return new MessageKick(isPenalty);
            case MessageMove.OPCODE:
                int[] moveArguments = MessageMove
                        .decodeArgumentsFromHash(hashedMessage);
                return new MessageMove(moveArguments[0] - MessageMove.OFFSET,
                        moveArguments[1] - MessageMove.OFFSET);
            case MessageRotate.OPCODE:
                int[] rotateArguments = MessageRotate
                        .decodeArgumentsFromHash(hashedMessage);
                return new MessageRotate(rotateArguments[0]
                        - MessageRotate.ANGLE_OFFSET, rotateArguments[1]);
            case MessageStop.OPCODE:
                int floatWheels = MessageStop
                        .decodeArgumentsFromHash(hashedMessage);
                return new MessageStop(floatWheels);
            default:
                // Unknown type
                return null;
        }
    }
}
