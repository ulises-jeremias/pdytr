/*
 * AskRemote.java
 * a) Looks up for the remote object
 * b) "Makes" the RMI
 */

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.Registry;

public class AskRemote {
        static Boolean verboseMode = false;
        static Boolean isSetBytesFlag = false;
        static Boolean listView = false;
        static Integer bytes = 0;
        static Integer initialPosition = 0;

        static String src = "";
        static String dest = "";
        static String host = "";

        public static void read(IfaceRemoteClass remote) {
                try {
                        byte endFile = 0;
                        int position = initialPosition;

                        while (endFile != 1) {
                                byte[] fileContent = remote.read(src, position);

                                endFile = fileContent[fileContent.length - 1];
                                fileContent = Arrays.copyOf(fileContent, fileContent.length - 1);
                                position += fileContent.length;

                                try {
                                        Files.write(Paths.get(dest), fileContent, StandardOpenOption.APPEND);
                                } catch (IOException e) {
                                        Files.createFile(Paths.get(dest));
                                        Files.write(Paths.get(dest), fileContent, StandardOpenOption.APPEND);
                                }
                        }

                        System.out.println("The file was successfully readed");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public static void write(IfaceRemoteClass remote) {
                try {
                        byte[] fileContent = Files.readAllBytes(Paths.get(src));
                        byte[] partialContent;

                        int fileSize = fileContent.length;
                        int bytesReaded = initialPosition;

                        StandardOpenOption openOption = StandardOpenOption.WRITE;

                        while (bytesReaded < fileSize) {
                                partialContent = Arrays.copyOfRange(fileContent, bytesReaded, fileSize);
                                int writeReturn = remote.write(dest, partialContent, openOption);
                                openOption = StandardOpenOption.APPEND;
                                bytesReaded += writeReturn;
                        }

                        System.out.println("The file was correctly written");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public static void list(IfaceRemoteClass remote) {
                try {
                        String listReturn = remote.list(src, listView);
                        System.out.println(listReturn);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public static void main(String[] argv) {
                long startTime, endTime;

                /* Look for hostname and msg length in the command line */
                if (argv.length < 1) {
                        System.out.printf("Usage: AskRemote\n"
                                        + "\t- write --src <local> --dest <remote>: Add a file from <local> to <remote> \n"
                                        + "\t- add --src <local> --dest <remote>: Add a file from <local> to <remote>\n"
                                        + "\t- read --dest <local> --src <remote>: Store a file from <local> to <remote>\n"
                                        + "\t- get --dest <local> --src <remote>: Store a file from <local> to <remote>\n"
                                        + "\t- list --src <remote/directory>: List files from <remote/directory>\n"
                                        + "\t- ls --src <remote/directory>: List files from <remote/directory>\n"
                                        + "\t- readwrite --src <local> --dest <remote>: Store a file from <local> to <remote> and backup at server\n"
                                        + "\t- rw --src <local> --dest <remote>: Store a file from <local> to <remote> and backup at server\n");
                        System.exit(1);
                }

                String command = argv[0];

                initParams(argv);

                /*
                 * Instead of reporting '--verbose' and '--brief' as they are encountered, we
                 * report the final status resulting from them.
                 */
                if (verboseMode)
                        System.out.println("Verbose mode activated");

                if (src.isEmpty() && !(command.equals("ls") || command.equals("list"))) {
                        System.err.println("Error: Specify a --src path");
                        System.exit(1);
                }

                if (dest.isEmpty()) {
                        if (verboseMode)
                                System.err.println("Warning: --dest setted to tmp1");

                        dest = "tmp1";
                }

                if (host.isEmpty())
                        host = "localhost";

                try {
                        String rname = "//" + host + ":" + Registry.REGISTRY_PORT + "/remote";
                        IfaceRemoteClass remote = (IfaceRemoteClass) Naming.lookup(rname);

                        switch (command) {
                        case "read":
                        case "get":
                                startTime = System.nanoTime();
                                read(remote);
                                endTime = System.nanoTime();

                                if (verboseMode)
                                        System.out.printf("Took: %d ms\n", (endTime - startTime) / 1000000);
                                break;

                        case "write":
                        case "add":
                                startTime = System.nanoTime();
                                write(remote);
                                endTime = System.nanoTime();

                                if (verboseMode)
                                        System.out.printf("Took: %d ms\n", (endTime - startTime) / 1000000);
                                break;

                        case "list":
                        case "ls":
                                startTime = System.nanoTime();
                                list(remote);
                                endTime = System.nanoTime();

                                if (verboseMode)
                                        System.out.printf("Took: %d ms\n", (endTime - startTime) / 1000000);
                                break;

                        case "rw":
                        case "readwrite":
                                startTime = System.nanoTime();
                                read(remote);
                                dest = "backup-" + dest;
                                write(remote);
                                endTime = System.nanoTime();

                                if (verboseMode)
                                        System.out.printf("Took: %d ms\n", (endTime - startTime) / 1000000);
                                break;

                        default:
                                System.err.println("Command unavailable");
                                break;
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public static void initParams(String[] argv) {
                int c;
                String arg;
                LongOpt[] longopts = new LongOpt[8];

                StringBuffer verboseModeSb = new StringBuffer("0");
                StringBuffer listViewSb = new StringBuffer("0");

                longopts[0] = new LongOpt("verbose", LongOpt.NO_ARGUMENT, verboseModeSb, '1');
                longopts[1] = new LongOpt("brief", LongOpt.NO_ARGUMENT, verboseModeSb, '0');
                longopts[2] = new LongOpt("all", LongOpt.NO_ARGUMENT, listViewSb, '1');
                longopts[3] = new LongOpt("bytes", LongOpt.REQUIRED_ARGUMENT, null, 'a');
                longopts[4] = new LongOpt("pos", LongOpt.REQUIRED_ARGUMENT, null, 'p');
                longopts[5] = new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'h');
                longopts[6] = new LongOpt("src", LongOpt.REQUIRED_ARGUMENT, null, 's');
                longopts[7] = new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 'd');

                Getopt g = new Getopt("flags", argv, "vbls:d:h:a:p:", longopts);
                g.setOpterr(false);

                while ((c = g.getopt()) != -1) {
                        switch (c) {
                        case 0:
                                /* If this option set a flag, do nothing else now. */
                                arg = g.getOptarg();
                                if (new Integer(longopts[g.getLongind()].getFlag().toString()) != 0)
                                        break;

                                System.out.println("option %s" + longopts[g.getLongind()].getName() + " with arg %s"
                                                + ((arg != null) ? arg : "null"));
                                break;
                        case 'v':
                                verboseModeSb = new StringBuffer("1");
                                break;
                        case 'b':
                                verboseModeSb = new StringBuffer("0");
                                break;
                        case 'l':
                                listViewSb = new StringBuffer("1");
                                break;
                        case 's':
                                src = g.getOptarg();
                                break;
                        case 'd':
                                dest = g.getOptarg();
                                break;
                        case 'h':
                                host = g.getOptarg();
                                break;
                        case 'a':
                                isSetBytesFlag = true;
                                bytes = new Integer(g.getOptarg());
                                break;
                        case 'p':
                                initialPosition = new Integer(g.getOptarg());
                                break;
                        case '?':
                                /* getopt_long already printed an error message. */
                                break;
                        default:
                                System.exit(0);
                        }
                }

                listViewSb.trimToSize();
                verboseModeSb.trimToSize();

                listView = new Integer(listViewSb.toString()) != 0;
                verboseMode = new Integer(verboseModeSb.toString()) != 0;

                for (int i = g.getOptind() + 1; i < argv.length; i++) {
                        System.out.println("Non option argv element: " + argv[i] + "\n");
                }
        }
}
