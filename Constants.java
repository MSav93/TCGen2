import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

  public static final ArrayList<String> unacceptedObsBehaviourTypes = new ArrayList<String>(
      Arrays.asList("EXTERNAL-INPUT", "EVENT", "EXTERNAL-OUTPUT", "SELECTION", "GUARD"));

  public static final ArrayList<String> acceptedUABehaviourTypes = new ArrayList<String>(
      Arrays.asList("EXTERNAL-INPUT", "EVENT", "EXTERNAL-OUTPUT"));
}
