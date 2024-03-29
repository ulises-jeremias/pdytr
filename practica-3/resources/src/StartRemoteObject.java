/*
 * StartRemoteObject.java
 * Starts the remote object. More specifically:
 * 1) Creates the object which has the remote methods to be invoked
 * 2) Registers the object so that it becomes avaliable
 */

import java.rmi.Naming;
import java.rmi.registry.Registry;

public class StartRemoteObject {

        public static void main(String args[]) {
                try {
                        /* Create ("start") the object which has the remote method */
                        FTServer robject = new FTServer();
                        /* Register the object using Naming.rebind(...) */
                        String rname = "//localhost:" + Registry.REGISTRY_PORT + "/remote";
                        Naming.rebind(rname, robject);
                } catch (Exception e) {
                        System.out.println("Hey, an error occurred at Naming.rebind");
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                }
        }

}
