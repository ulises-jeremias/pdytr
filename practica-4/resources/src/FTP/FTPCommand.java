import jade.core.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UnicastRemoteObject;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public class FTPCommand {
        public static byte[] read(String path, int position, int currentSize, long fileSize)
        {
                try {
                        int chunck = 1024;
                        int noBytes = ((int) fileSize - currentSize) < chunck
                                ? (int) (fileSize - currentSize)
                                : chunck;
                        
                        byte[] contents = new byte[noBytes];

                        System.out.printf("Reading %d bytes from %d\n", noBytes, currentSize);

                        InputStream in = new FileInputStream(path);
                        in.skip(currentSize);
                        in.read(contents, 0, noBytes);
                        in.close();

                        return contents;
                } catch(IOException e) {
                        System.out.println(e);
                        return new byte[0];
                }
        }

        public static int write(String path, byte[] data, int currentSize, long fileSize)
        {
                try {
                        try {
                                Files.write(Paths.get(path), data,StandardOpenOption.APPEND);
                        } catch (IOException e) {
                                Files.createFile(Paths.get(path));
                                Files.write(Paths.get(path), data, StandardOpenOption.APPEND);
                        }
                        
                        System.out.printf("Written %d of %d bytes\n", currentSize, fileSize);
                        return data.length;
                } catch (IOException e) {
                        System.out.println(e.toString());
                        return -1;
                }
        }

        public static String list(String path)
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
}