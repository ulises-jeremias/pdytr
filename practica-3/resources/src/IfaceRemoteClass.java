/*
 * IfaceRemoteClass.java
 * Interface defining only one method which can be invoked remotely
 *
 */

/* Needed for defining remote method/s */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.nio.file.StandardOpenOption;

/* This interface will need an implementing class */
public interface IfaceRemoteClass extends Remote {

      /* It will be possible to invoke this method from an application in other JVM */
      public byte[] read(String path, int pos) throws RemoteException;

      /* It will be possible to invoke this method from an application in other JVM */
      public int write(String path, byte[] data, StandardOpenOption openOption) throws RemoteException;

      /* It will be possible to invoke this method from an application in other JVM */
      public String list(String path, Boolean listView) throws RemoteException;
}
