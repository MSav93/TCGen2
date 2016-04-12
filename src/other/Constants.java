package other;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public final class Constants {

  public static final String version = "0.1";

  public static final String noiTabName = "Nodes of Interest";

  public static final String cpTabName = "CheckPoints";

  public static final String observablesTabName = "Observable Responses";

  public static final String userActionsTabName = "User Actions/External Inputs";

  public static final String joiningTabName = "Join Test Cases";

  public static final String htmlTabSpacing = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

  public static final int timeout = 5000;

  public static final Color selectedColour = new Color(100, 215, 115);

  public static final Color notSelectedColour = new Color(255, 255, 255);
  
  public static final Color warningColour = new Color(235, 235, 60);

  public static final Color unavailableColour = new Color(225, 30, 75);

  // 8 spaces
  public static final String tabSpacing = "        ";

  public static final ArrayList<String> acceptedORBehaviourTypes = new ArrayList<String>(
      Arrays.asList("STATE-REALISATION", "EXTERNAL-OUTPUT", "INTERNAL-OUTPUT"));

  public static final ArrayList<String> acceptedUABehaviourTypes =
      new ArrayList<String>(Arrays.asList("EXTERNAL-INPUT", "EVENT"));
  
  public static final ArrayList<String> acceptedUAFlags =
      new ArrayList<String>(Arrays.asList("KILL", "SYNCHRONISATION", ""));
  
  public static final ArrayList<String> acceptedORFlags =
      new ArrayList<String>(Arrays.asList("KILL", "SYNCHRONISATION", ""));

  public static final ArrayList<String> nodeFlags =
      new ArrayList<String>(Arrays.asList("REVERSION", "REFERENCE", "KILL", "SYNCHRONISATION"));

  public static final ArrayList<String> nodeFlagSymbols =
      new ArrayList<String>(Arrays.asList("^", "=>", "--", "="));
}
