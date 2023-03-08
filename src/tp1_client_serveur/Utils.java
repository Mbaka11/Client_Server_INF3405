package tp1_client_serveur;


public class Utils {
	
	/**
	 * Extracts the command prefix from a command string (the first word)
	 * @param command
	 * @return
	 */
	static public String extractCommandPrefix(String command) {
		String prefix = command.split("\\s+", 2)[0];
		return prefix;
	}
	
	
	/**
	 * Extracts the command argument from a command string (the second word)
	 * @param command
	 * @return
	 */
	static public String extractCommandArgument(String command) {
		String argument = "";
		try {
			argument = command.split("\\s+", 2)[1];
		} catch (IndexOutOfBoundsException e) {
			argument = "";
		}
		return argument;
	}
	
	
}
