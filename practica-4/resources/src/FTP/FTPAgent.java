import jade.core.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public class FTPAgent extends Agent
{
        private Location origen;

        private String method;
        private String sourcePath;
        private String destinationPath;
        private String files;

        private byte[] file = null;
        private long fileSize;
        private int currentSize = 0;

        public void setup()
        {
                Object[] args = getArguments();
                this.getopt(args);

                this.origen = here();

                try {
                        ContainerID destination = new ContainerID("Main-Container", null);
                        System.out.println("Migrating the agent " + destination.getID());
                        doMove(destination);
                } catch (Exception e) {
                        System.out.println("Error: It was not posible to migrate the agent!");
                        System.out.println(e);
                }
        }

        protected void afterMove()
        {
                Location here = here();

                    switch (this.method) {
                    case "write":
                            this.writeAfterMove(here);
                            break;
                    case "read":
                            this.readAfterMove(here);
                            break;
                    case "list":
                            this.listAfterMove(here);
                            break;
                    }
        }

        private void writeAfterMove(Location here)
        {
            if (!here.getName().equals(this.origen.getName())) {
                    FTPCommand.write(this.destinationPath, this.file, this.currentSize, this.fileSize);
                    doMove(new ContainerID(this.origen.getName(), null));
            } else if (this.fileSize > this.currentSize) {
                    this.file = FTPCommand.read(this.sourcePath, this.currentSize, this.currentSize, this.fileSize);
                    this.currentSize += this.file.length;
                    System.out.printf("%d of %d bytes readed\n", this.file.length, this.fileSize);
                    doMove(new ContainerID("Main-Container", null));
            } else {
                    System.out.println("The file " + this.sourcePath + " was written in the remote directory " + this.destinationPath);
            }
        }

        private void readAfterMove(Location here)
        {
            if (!here.getName().equals(this.origen.getName())) {
                    this.file = FTPCommand.read(this.destinationPath, this.currentSize, this.currentSize, this.fileSize);
                    this.currentSize += this.file.length;
                    System.out.printf("Reading %d bytes of %d\n", this.file.length, this.fileSize);
                    doMove(new ContainerID(this.origen.getName(), null));
                    return;
            }

            try {
                try {
                        Files.write(Paths.get(this.sourcePath), this.file,StandardOpenOption.APPEND);
                        System.out.printf("Writing %d bytes of %d\n", this.file.length, this.fileSize);
                } catch (IOException e) {
                        Files.createFile(Paths.get(this.sourcePath));
                        Files.write(Paths.get(this.sourcePath), this.file,StandardOpenOption.APPEND);
                } finally {
                        if (this.fileSize > this.currentSize) {
                                System.out.println("Bytes left!");
                                doMove(new ContainerID("Main-Container", null));
                        } else {
                                System.out.println("The file " + this.sourcePath + " was readed correctly and successfuly loaded in " + this.destinationPath);
                        }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void listAfterMove(Location here)
        {
            if (!here.getName().equals(this.origen.getName())) {
                    this.files = FTPCommand.list(this.destinationPath);
                    doMove(new ContainerID(this.origen.getName(), null));
                    return;
            }
            
            System.out.println(this.files);
        }

        private void getopt(Object[] args){
                try {
                        switch ((String) args[0]) {
                        case "write":
                        case "read":
                                if (args.length != 3) {
                                        System.out.println("3 argument needed: command, local directory and remote directory");
                                        System.exit(1);
                                }
                                
                                this.method          = (String) args[0];
                                this.sourcePath      = (String) args[1];
                                this.destinationPath = (String) args[2];

                                if (this.method.equals("write")) {
                                        this.file = FTPCommand.read(this.destinationPath, this.currentSize, this.currentSize, this.fileSize);
                                        this.currentSize += this.file.length;
                                        this.fileSize = Files.size(Paths.get(this.sourcePath));
                                } else {
                                        this.fileSize = Files.size(Paths.get(this.destinationPath));
                                }
                                break;
                        case "list":
                                if (args.length != 1) {
                                        System.out.println("2 argument needed: command and directory");
                                        System.exit(1);
                                } else {
                                        this.method          = (String) args[0];
                                        this.destinationPath = (String) args[1];
                                }
                                break;

                        default: 
                                System.out.printf("Command %s unavailable\n", (String) args[0]);
                                System.exit(1);
                                break;
                        }
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
