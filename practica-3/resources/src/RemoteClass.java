/*
 * RemoteClass.java
 * Just implements the RemoteMethod interface as an extension to
 * UnicastRemoteObject
 *
 */

/* Needed for implementing remote method/s */
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileDescriptor;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.io.IOException;

/* This class implements the interface with remote methods */
public class RemoteClass extends UnicastRemoteObject implements IfaceRemoteClass {

        private static final long serialVersionUID = 1L;

        protected RemoteClass() throws RemoteException {
                super();
        }

        /* Remote method implementation */
        @Override
        public byte[] read(String path, int pos, int bytes) throws RemoteException {
                try {
                        RandomAccessFile file = new RandomAccessFile(Paths.get(path).toString(), "r");
                        FileDescriptor fd = file.getFD();
                        FileInputStream fis = new FileInputStream(fd);

                        file.seek(pos);

                        byte[] contents = new byte[Math.min(bytes, fis.available())];
                        byte fileEnd = 1;

                        fis.read(contents, 0, Math.min(bytes, fis.available()));

                        fileEnd = (byte) (fis.available() == 0 ? 1 : 0);
                        fis.close();

                        byte[] sizeAndContent = Arrays.copyOf(contents, contents.length + 1);
                        sizeAndContent[contents.length] = fileEnd;

                        return sizeAndContent;
                } catch (Exception e) {
                        System.err.println(e);
                        return new byte[0];
                }
        }

        /* Remote method implementation */
        @Override
        public int write(String path, byte[] data, int bytes, StandardOpenOption openOption) throws RemoteException {
                try {
                        if (data.length > bytes)
                                data = Arrays.copyOf(data, bytes);

                        try {
                                Files.write(Paths.get("store/" + path), data, openOption);
                        } catch (IOException e) {
                                createFile("store", path);
                                Files.write(Paths.get("store/" + path), data, StandardOpenOption.APPEND);
                        }

                        return data.length;
                } catch (Exception e) {
                        System.err.println(e.toString());
                        return -1;
                }
        }

        /* Remote method implementation */
        @Override
        public String list(String path, Boolean listView) throws RemoteException {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get("store/" + path))) {
                        String directoryPaths = "";
                        System.out.printf("Listing files in %s\n", "store/" + path);

                        for (Path p : paths) {
                                directoryPaths += p.getFileName().toString();
                                directoryPaths += listView ? "\n" : " ";
                        }

                        return directoryPaths.substring(0, directoryPaths.length() - 1);
                } catch (Exception e) {
                        System.err.println(e);
                        return e.toString();
                }
        }

        private void createFile(String directoryName, String fileName) throws IOException {
                File directory = new File(directoryName);

                if (!directory.exists()) {
                        directory.mkdir();
                        // If you require it to make the entire directory path including parents,
                        // use directory.mkdirs(); here instead.
                }

                Files.createFile(Paths.get(directoryName + "/" + fileName));
        }
}
