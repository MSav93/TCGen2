package other;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public final class Constants {

  public static final String version = "0.3";

  public static final String noiTabName = "Nodes of Interest";

  public static final String cpTabName = "CheckPoints";

  public static final String observablesTabName = "Observable Responses";

  public static final String userActionsTabName = "User Actions/External Inputs";

  public static final String joiningTabName = "Join Test Cases";

  public static final String htmlTabSpacing = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

  public static final int timeout = 5000;

  public static final Color nodeSelectedColour = new Color(50, 235, 95);

  public static final Color testCaseSelectedColour = new Color(50, 190, 235);

  public static final Color notSelectedColour = new Color(255, 255, 255);

  public static final Color preambleColour = new Color(235, 235, 60);

  public static final Color unavailableColour = new Color(225, 30, 75);

  public static final Color immediatelyAvailableColour = new Color(50, 235, 95);

  // 8 spaces
  public static final String tabSpacing = "        ";

  public static final ArrayList<String> acceptedCPBehaviourTypes =
      new ArrayList<String>(Arrays.asList("STATE-REALISATION"));

  public static final ArrayList<String> acceptedNOIBehaviourTypes = new ArrayList<String>(
      Arrays.asList("STATE-REALISATION", "EXTERNAL-OUTPUT", "INTERNAL-OUTPUT"));

  public static final ArrayList<String> acceptedORBehaviourTypes = new ArrayList<String>(
      Arrays.asList("STATE-REALISATION", "EXTERNAL-OUTPUT", "INTERNAL-OUTPUT", "EXTERNAL-INPUT"));

  public static final ArrayList<String> acceptedUABehaviourTypes =
      new ArrayList<String>(Arrays.asList("EXTERNAL-INPUT", "EVENT"));

  public static final ArrayList<String> acceptedCPFlags =
      new ArrayList<String>(Arrays.asList("KILL", "SYNCHRONISATION", "MAY", "START NEW", ""));

  public static final ArrayList<String> acceptedNOIFlags =
      new ArrayList<String>(Arrays.asList("KILL", "SYNCHRONISATION", "MAY", "START NEW", ""));

  public static final ArrayList<String> acceptedUAFlags =
      new ArrayList<String>(Arrays.asList("KILL", "SYNCHRONISATION", "MAY", "START NEW", ""));

  public static final ArrayList<String> acceptedORFlags =
      new ArrayList<String>(Arrays.asList("KILL", "SYNCHRONISATION", "MAY", "START NEW", ""));

  public static final ArrayList<String> nodeReversionFlags =
      new ArrayList<String>(Arrays.asList("REVERSION", "REFERENCE"));

  public static final ArrayList<String> nodeFlags = new ArrayList<String>(
      Arrays.asList("REVERSION", "REFERENCE", "KILL", "SYNCHRONISE", "MAY", "START NEW"));

  public static final ArrayList<String> nodeFlagSymbols =
      new ArrayList<String>(Arrays.asList("^", "=>", "--", "=", "%", "^^"));
}
