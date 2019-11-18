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
        private String action;
        private String sourcePath;
        private String destinationPath;
        private byte[] file = null;
        private String files;
        private Location origen;
        private long fileSize;
        private int actualSize = 0;

        public void setup()
        {
                //Chequeo parametros y seteo variables
                Object[] args = getArguments();
                this.checkAndSetArguments(args);

                //informacion del agente
                this.origen = here();

                try {
                        //migracion a otro container
                        ContainerID destino = new ContainerID("Main-Container", null);
                        System.out.println("Migrating the agent " + destino.getID());
                        doMove(destino);
                } catch (Exception e) {
                        System.out.println("Error: It was not posible to migrate the agent!");
                        System.out.println(e);
                }
        }

        protected void afterMove()
        {
                //Muestro info del container actual
                Location actual = here();

                try {
                        switch (this.action) {
                        case "write":
                                if (!actual.getName().equals(this.origen.getName())) {
                                        this.write(this.destinationPath,this.file);
                                        doMove(new ContainerID(this.origen.getName(), null));
                                } else {
                                        if (this.fileSize > this.actualSize) {
                                                this.file = this.read(this.sourcePath, this.actualSize);
                                                System.out.printf("%d of %d bytes readed\n", this.file.length, this.fileSize);
                                                doMove(new ContainerID("Main-Container", null));
                                        } else {
                                                System.out.println("The file " + this.sourcePath + " was written in the remote directory " + this.destinationPath);
                                        }
                                }
                                break;
                        case "read":
                                if (!actual.getName().equals(this.origen.getName())) {
                                        this.file = this.read(this.destinationPath, this.actualSize);
                                        System.out.printf("Reading %d bytes of %d\n", this.file.length, this.fileSize);
                                        doMove(new ContainerID(this.origen.getName(), null));
                                } else {
                                        try {
                                                Files.write(Paths.get(this.sourcePath), this.file,StandardOpenOption.APPEND);
                                                System.out.printf("Writing %d bytes of %d\n", this.file.length, this.fileSize);
                                        }
                                        catch (IOException e) {
                                                Files.createFile(Paths.get(this.sourcePath));
                                                Files.write(Paths.get(this.sourcePath), this.file,StandardOpenOption.APPEND);

                                        } finally {
                                                if (this.fileSize > this.actualSize) {
                                                        System.out.println("Bytes left!");
                                                        doMove(new ContainerID("Main-Container", null));
                                                } else {
                                                        System.out.println("The file " + this.destinationPath + " was readed correctly and successfuly loaded in " + this.sourcePath);
                                                }
                                        }
                                }
                                break;
                        case "list":
                                if (!actual.getName().equals(this.origen.getName())) {
                                        this.files = this.list(this.destinationPath);
                                        doMove(new ContainerID(this.origen.getName(), null));
                                } else {
                                        System.out.println(this.files);
                                }
                                break;
                        }
                } catch (IOException e) {
                        System.out.println(e);
                }

        }

        private byte[] read(String path, int position)
        {
                try {
                        int chunck = 2000;
                        int noBytes = ((int) this.fileSize - this.actualSize) < chunck ? (int) (this.fileSize - this.actualSize) : chunck;
                        System.out.printf("Reading %d bytes from %d\n", noBytes, this.actualSize);
                        InputStream in = new FileInputStream(path);
                        byte[] contents = new byte[noBytes];
                        in.skip(this.actualSize);
                        in.read(contents, 0, noBytes);
                        this.actualSize += contents.length;
                        return contents;
                } catch(IOException e) {
                        System.out.println(e);
                        return new byte[0];
                }
        }

        private int write(String path, byte[] data)
        {
                try {
                        try {
                                Files.write(Paths.get(path), data,StandardOpenOption.APPEND);
                        } catch (IOException e) {
                                Files.createFile(Paths.get(path));
                                Files.write(Paths.get(path), data, StandardOpenOption.APPEND);
                        }
                        System.out.printf("Written %d of %d bytes\n", this.actualSize, this.fileSize);
                        return data.length;
                } catch (IOException e) {
                        System.out.println(e.toString());
                        return -1;
                }
        }

        private String list(String path)
        {
                String directoryPaths = "";

                try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(path))) {
                        System.out.printf("Listing files in %s\n", path);

                        for (Path p : paths) {
                                directoryPaths += p.toString();
                                directoryPaths += "\n";
                        }
                } catch(Exception e) {
                        System.out.println(e);
                }

                return directoryPaths;
        }

        private void checkAndSetArguments(Object[] args){
                try {
                        switch ((String) args[0]) {
                        case "write":
                        case "read":
                                if (args.length != 3) {
                                        System.out.println("3 argument needed: command, local directory and remote directory");
                                        System.exit(1);
                                } else {
                                        this.action          = (String) args[0];
                                        this.sourcePath      = (String) args[1];
                                        this.destinationPath = (String) args[2];

                                        if (this.action.equals("write")) {
                                                this.file = this.read(this.destinationPath, this.actualSize);
                                                this.fileSize = Files.size(Paths.get(this.sourcePath));
                                        } else {
                                                this.fileSize = Files.size(Paths.get(this.destinationPath));
                                        }
                                }
                                break;
                        case "list":
                                if (args.length != 1) {
                                        System.out.println("2 argument needed: command and directory");
                                        System.exit(1);
                                } else {
                                        this.action          = (String) args[0];
                                        this.destinationPath = (String) args[1];
                                }
                                break;

                        default: System.out.printf("Command %s unavailable\n", (String) args[0]);
                                System.exit(1);
                                break;
                        }
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
