package tk.slicecollections.maxteer.nms.network;

/**
 * Compile-time API contract for the newer mCore packet access helper.
 * Gradle excludes this class from mLobby.jar; mCore supplies the implementation at runtime.
 */
public final class NMSPacketAccess {

  private NMSPacketAccess() {}

  public static Object get(Object target, Class<?> fieldType, int index) {
    throw new UnsupportedOperationException("Compile-time contract only");
  }

  public static void set(Object target, Class<?> fieldType, int index, Object value) {
    throw new UnsupportedOperationException("Compile-time contract only");
  }
}
