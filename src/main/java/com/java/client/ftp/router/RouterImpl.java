package com.java.client.ftp.router;
import com.java.client.ftp.enums.CommandOfClient;
import com.java.client.ftp.handle.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RouterImpl implements Router {

    private final FileCommand fileCommand;
    private final DirectoryCommand directoryCommand;
    private final CommonCommand commonCommand;
    private final DataTransferCommand dataTransferCommand;
    private final ConnectionCommand connectionCommand;
    private final LocalCommand localCommand;

    @Override
    public void routeCommand(String commandString) {

        try {
            String[] commandParts = commandString.split(" ");
            CommandOfClient command = processCommandString(commandParts[0].trim());
            if (command == null) {
                System.err.print("Invalid command: " + commandParts[0]);
                return;
            }
            String[] remainingParts = getRemainingParts(commandParts);
            switch (command) {

                // file
                case PUT:
                    fileCommand.put(remainingParts[0]);
                    break;
                case MPUT:
                    fileCommand.multiPut(remainingParts);
                    break;
                case GET:
                    fileCommand.get(remainingParts[0]);
                    break;
                case MGET:
                    fileCommand.multiGet(remainingParts);
                    break;
                case DELETE:
                    fileCommand.delete(remainingParts[0]);
                    break;
                case MDELETE:
                    fileCommand.multiDelete(remainingParts);
                    break;
                case SEND:
                    fileCommand.send(remainingParts[0], remainingParts[1]); // check sau
                    break;
                case RECV:
                    fileCommand.receive(remainingParts[0], remainingParts[1]);
                    break;
                case APPEND:
                    fileCommand.append(remainingParts[0], remainingParts[1]);
                    break;

                // folder
                case MKDIR:
                    directoryCommand.makeDirectory(remainingParts[0]);
                    break;
                case RMDIR:
                    directoryCommand.removeDirectory(remainingParts[0]);
                    break;
                case CD:
                    directoryCommand.changeDirectory(remainingParts[0]);
                    break;
                case PWD:
                    directoryCommand.printWorkingDirectory();
                    // common
                    break;
                case DIR:
                    commonCommand.listDetail(remainingParts[0]);
                    break;
                case MDIR:
                    commonCommand.listDetailAndStore(remainingParts[0], remainingParts[1]);
                    break;
                case LS:
                    commonCommand.listName(remainingParts[0]);
                    break;
                case MLS:
                    commonCommand.listNameAndStore(remainingParts[0], remainingParts[1]);
                    break;
                case RENAME:
                    commonCommand.rename(remainingParts[0], remainingParts[1]);
                    break;

                // connection
                case OPEN:
                    connectionCommand.openConnection(remainingParts[0], Integer.parseInt(remainingParts[1]));
                    break;
                case CLOSE:
                    connectionCommand.closeConnection();
                    break;
                case DISCONNECT:
                    connectionCommand.disconnect();
                    break;
                case BYE:
                    connectionCommand.bye();
                    break;
                case QUIT:
                    connectionCommand.quit();
                    break;

                // transfer
                case ASCII:
                    dataTransferCommand.setAsciiMode();
                    break;
                case BINARY:
                    dataTransferCommand.setBinaryMode();
                    break;

                // remote help
                case REMOTEHELP:
                    break;

                // connect
                case FTP:
                    if (remainingParts.length > 1) {
                        connectionCommand.openConnection(remainingParts[0], Integer.parseInt(remainingParts[1]));
                    }
                    else {
                        connectionCommand.openConnection(null, null);
                    }
                    break;

                case DEBUG:
                    localCommand.debug();
                    break;
                case ACTIVE:
                    localCommand.active();
                    break;
                case PASSIVE:
                    localCommand.passive();
                    break;
                case STATUS:
                    localCommand.showStatus();
                    break;
//            // extend
//            case QUOTE:
//
//                break;

//            // local
//            case LCD:
//
//                break;
//            case LITERAL:
//
//                break;
//            case PROMPT:
//
//                break;
//            case GLOB:
//
//                break;
//            case TRACE:
//
//                break;
//            case VERBOSE:
//
//                break;
//            case STATUS:
//
//                break;
//            case HASH:
//
//                break;
//            case HELP:
//
//                break;
            }
        }catch (Exception e) {
            System.err.print("command error");
        }
    }

    private CommandOfClient processCommandString(String commandString) {
        try {
            return CommandOfClient.valueOf(commandString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;  // or throw exception
        }
    }

        private String[] getRemainingParts(String[] commandParts) {
        if (commandParts.length > 1) {
            String[] remainingParts = new String[commandParts.length - 1];
            System.arraycopy(commandParts, 1, remainingParts, 0, commandParts.length - 1);
            return remainingParts;
        }
        return new String[0]; // or throw ex
    }
}