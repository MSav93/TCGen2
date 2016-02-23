import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import jxl.Cell;
import jxl.CellView;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.format.PageOrientation;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import other.AlphanumComparator;
import other.TestPath;
import other.TestPathCell;
import other.TestPathModel;
import tree.BTNode;
import tree.NodeData;
import util.BTModelReader;

import com.SBCLPipe;

public class Main extends JFrame {

  /**
   * Written by Soh Wei Yu. You're welcome to contact me with any questions: sohweiyu@outlook.com
   */
  private static final long serialVersionUID = 1L;
  private JPanel contentPane;
  private ArrayList<ArrayList<String[]>> componentList = new ArrayList<ArrayList<String[]>>();
  private ArrayList<ArrayList<String[]>> AllTSSComponents = new ArrayList<ArrayList<String[]>>();
  private ArrayList<ArrayList<String[]>> AllORComponents = new ArrayList<ArrayList<String[]>>();
  private ArrayList<ArrayList<String[]>> AllUAComponents = new ArrayList<ArrayList<String[]>>();
  private String filenameStr = "";

  LinkedHashMap<String, ArrayList<ArrayList<Element>>> testPaths =
      new LinkedHashMap<String, ArrayList<ArrayList<Element>>>();

  HashMap<String, ArrayList<NodeData>> mapToTag2 = new HashMap<>(); // map block index to tag
  public LinkedHashMap<String, String[]> listOfTestPaths = new LinkedHashMap<>();

  private ArrayList<String[]> BTArray = new ArrayList<String[]>();

  private JComboBox<String> cmbStartState = new JComboBox<String>();
  private JComboBox<String> cmbEndState = new JComboBox<String>();
  private JComboBox<String> cmbInitialState = new JComboBox<String>();
  private JComboBox<String> cmbComponent = new JComboBox<String>();
  private JComboBox<String> cmbBehaviour = new JComboBox<String>();
  private JComboBox<String> cmbObsComp = new JComboBox<String>();
  private JComboBox<String> cmbObsBehaviour = new JComboBox<String>();
  private JComboBox<String> cmbUAComp = new JComboBox<String>();
  private JComboBox<String> cmbUABehaviour = new JComboBox<String>();
  private final JCheckBox chkTSS = new JCheckBox("This is a Target System State");
  private final JCheckBox chckbxAppearPreAmble =
      new JCheckBox(
          "This condition should appear in the Pre-Amble/This is an External Input not under user's control");
  private final JTextArea txtObs = new JTextArea();
  private final JTextArea txtUA = new JTextArea();
  private final JLabel lblSelectedNodes = new JLabel("Select nodes of interest:");
  private final TextArea taTSS = new TextArea();
  private final TextArea taObservableResponses = new TextArea();
  private final TextArea taUserActions = new TextArea();

  private JRadioButton rdbtnGenerateAll = new JRadioButton(
      "I want to generate test cases for all possible Start & End states");
  private JRadioButton rdbtnGenerateComponentTypes = new JRadioButton(
      "I want to generate test cases by component types");
  private JRadioButton rdbtnIWantTo = new JRadioButton(
      "I want to generate test cases by specific Start & End states");
  private ButtonGroup bg = new ButtonGroup();

  public static int instanceCount = 0;

  private int rangeNum = 0;

  private static Main frame = null;

  private Boolean isLoaded = false;
  private String[] range = new String[3];

  private String[] comboBoxstr = new String[0];
  private String[] comboBoxstrNonReversion = new String[0];

  HashMap<String, Boolean> nodesOfInterest = new HashMap<>();
  HashMap<String, Boolean> chosenTSS = new HashMap<>();
  HashMap<String, String> observableResponses = new HashMap<>();
  HashMap<String, String[]> userActions = new HashMap<>();
  HashMap<String, String> mapToTag = new HashMap<>();
  HashMap<String, String> mapToTagName = new HashMap<>();
  HashMap<String, Integer> tagToIndexMap = new HashMap<>(); // map tag to block index

  private SBCLPipe sbcl = new SBCLPipe();
  private BTNode root;
  private TreeMap<Integer, BTNode> indexToNodeMap = new TreeMap<Integer, BTNode>();
  private HashMap<Integer, NodeData> tagToNodeDataMap = new HashMap<Integer, NodeData>();
  private HashMap<String, ArrayList<NodeData>> compToBehaviourMap =
      new HashMap<String, ArrayList<NodeData>>();

  private String initialNode;
  private String startNode;
  private String endNode;

  private JTable noiTable;

  private JTabbedPane tabbedPane;
  private JButton btnGenerateTestPaths;



  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    // try {
    // Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd BTAnalyser && start.cmd\"");
    // } catch (IOException e1) {
    // }
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          Runtime.getRuntime().exec("taskkill /F /IM sbcl.exe");
        } catch (IOException e) {
          printErrorMessage("error|3|");
        }
      }
    }));
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          frame = new Main();
          frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
          frame.setVisible(true);
          instanceCount++;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Write label that shows configured TSS nodes.
   */
  public void updateTSSTA() {
    taTSS.setText("");
    for (Entry<String, Boolean> node : chosenTSS.entrySet()) {
      String key = node.getKey();
      Boolean value = node.getValue();

      if (value == true) {
        if (taTSS.getText().equals("")) {
          taTSS.setText(taTSS.getText() + key);
        } else {
          taTSS.setText(taTSS.getText() + ", \n" + key);
        }
      }
    }
    taTSS.setText(taTSS.getText().replaceAll(";", ": "));
  }


  /**
   * Load the observable response for selected component.
   */
  public void updateOTA() {
    taObservableResponses.setText("");
    for (Entry<String, String> component : observableResponses.entrySet()) {
      String key = component.getKey();
      String value = component.getValue();

      if (!value.equals("")) {
        if (taObservableResponses.getText().equals("")) {
          taObservableResponses.setText(taObservableResponses.getText() + key);
        } else {
          taObservableResponses.setText(taObservableResponses.getText() + ", \n" + key);
        }
      }
    }
    taObservableResponses.setText(taObservableResponses.getText().replaceAll(";", ": "));
  }


  /**
   * Load the user action for selected component.
   */
  public void updateUATA() {
    taUserActions.setText("");
    for (Entry<String, String[]> component : userActions.entrySet()) {
      String key = component.getKey();
      String[] value = component.getValue();

      if (!value[0].equals("")) {
        if (taUserActions.getText().equals("")) {
          taUserActions.setText(taUserActions.getText() + key);
        } else {
          taUserActions.setText(taUserActions.getText() + ", \n" + key);
        }
      }
    }
    taUserActions.setText(taUserActions.getText().replaceAll(";", ": "));
  }


  /**
   * Clear all previous configurations from memory.
   */
  public void clearEverything() {
    int count = noiTable.getRowCount();
    for (int i = 0; i < count; i++) {
      ((DefaultTableModel) noiTable.getModel()).removeRow(0);
    }
    cmbEndState.removeAllItems();
    cmbStartState.removeAllItems();
    cmbInitialState.removeAllItems();
    cmbComponent.removeAllItems();
    cmbBehaviour.removeAllItems();
    cmbObsComp.removeAllItems();
    cmbObsBehaviour.removeAllItems();
    cmbUAComp.removeAllItems();
    cmbUABehaviour.removeAllItems();

    componentList = new ArrayList<ArrayList<String[]>>();
    AllTSSComponents = new ArrayList<ArrayList<String[]>>();
    AllORComponents = new ArrayList<ArrayList<String[]>>();
    AllUAComponents = new ArrayList<ArrayList<String[]>>();
    testPaths = new LinkedHashMap<String, ArrayList<ArrayList<Element>>>();

    listOfTestPaths = new LinkedHashMap<>();

    nodesOfInterest = new HashMap<>();
    chosenTSS = new HashMap<>();
    observableResponses = new HashMap<>();
    userActions = new HashMap<>();
    mapToTag = new HashMap<>();
    mapToTagName = new HashMap<>();
    mapToTag2 = new HashMap<>();
    tagToIndexMap = new HashMap<>();
    indexToNodeMap = new TreeMap<>();

    BTArray = new ArrayList<String[]>();
    range = new String[3];
    comboBoxstr = new String[0];
    comboBoxstrNonReversion = new String[0];

    chkTSS.setSelected(false);

    chckbxAppearPreAmble.setSelected(false);
    txtObs.setText("");
    txtUA.setText("");

    rdbtnGenerateAll.setSelected(false);
    rdbtnGenerateComponentTypes.setSelected(false);
    rdbtnIWantTo.setSelected(false);

    // taNOI.setText("");
    taTSS.setText("");

    taObservableResponses.setText("");
    taUserActions.setText("");

    filenameStr = "";

    updateTSSTA();
    updateOTA();
    updateUATA();
  }


  /**
   * Populate configuration in Range.
   */
  public void updateRangeTab() {
    comboBoxstr = new String[indexToNodeMap.size()];
    comboBoxstrNonReversion = new String[indexToNodeMap.size()];
    int i = 0;
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        for (Entry<String, Boolean> selectedTSS : chosenTSS.entrySet()) {
          String tss = selectedTSS.getKey();
          Boolean selected = selectedTSS.getValue();
          if (selected) {
            String[] tssParts = tss.split(";");
            if (node.getComponent().equals(tssParts[0]) && node.getBehaviour().equals(tssParts[1])) {

              if (node.getBehaviourType().equals("STATE-REALISATION")) {
                comboBoxstr[i] =
                    "Node " + node.getTag() + ": " + node.getComponent() + " - "
                        + node.getBehaviour() + " [" + node.getBehaviourType() + "]";
                i++;

              }
              // TODO
              // the reversion flag, marked by ‘ ^ ’, leads to a looping behaviour back to the
              // closest
              // matching ancestor node and all behaviour started after the matching ancestor node
              // is
              // terminated.
              // The reversion flag can only be set for leaf nodes. Another example is the thread
              // kill
              // flag, marked by ‘=
              // ’, enforces the control flow to wait until all other synchronisation points
              // (matching
              // nodes that also have
              // the synchronisation flag set) are reached.
            }
          }
        }
      }
    }
    comboBoxstr = clean(comboBoxstr);
    Arrays.sort(comboBoxstr, new AlphanumComparator());
    cmbEndState.setModel(new DefaultComboBoxModel<String>(comboBoxstr));
    cmbStartState.setModel(new DefaultComboBoxModel<String>(comboBoxstr));
    cmbInitialState.setModel(new DefaultComboBoxModel<String>(comboBoxstr));
    setRange();
  }

  /**
   * Populate configuration in Range by component types.
   */
  public void updateRangeTabComponent() {
    Set<String> set = new HashSet<String>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        for (Entry<String, Boolean> selectedTSS : chosenTSS.entrySet()) {
          String tss = selectedTSS.getKey();
          Boolean selected = selectedTSS.getValue();
          if (selected) {
            String[] tssParts = tss.split(";");
            if (node.getComponent().equals(tssParts[0]) && node.getBehaviour().equals(tssParts[1])) {
              if (node.getBehaviourType().equals("STATE-REALISATION")) {
                set.add(node.getComponent() + " - " + node.getBehaviour());
              }
            }
          }
        }
      }
    }
    comboBoxstr = set.toArray(new String[0]);
    Arrays.sort(comboBoxstr, new AlphanumComparator());

    cmbStartState.setModel(new DefaultComboBoxModel<String>(comboBoxstr));
    cmbEndState.setModel(new DefaultComboBoxModel<String>(comboBoxstr));
  }


  /**
   * Open folder containing test cases.
   */
  public void openFolder() {
    try {
      String path =
          System.getProperty("user.dir") + System.getProperty("file.separator") + "test-cases";
      Path thePath = Paths.get(path);
      if (Files.exists(thePath)) {
        Desktop.getDesktop().open(new File(path));
      } else {
        boolean success = (new File(path)).mkdirs();
        if (!success) {
          // Directory creation failed
          JOptionPane.showMessageDialog(
              null,
              "Unable to create folder. Please ensure write access is available in "
                  + System.getProperty("user.dir"));
        } else {
          try {
            Desktop.getDesktop().open(
                new File(System.getProperty("user.dir") + System.getProperty("file.separator")
                    + "test-cases"));
          } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    }
  }

  /**
   * Record the selected option for range.
   */
  public void setRange() {
    initialNode = cmbInitialState.getSelectedItem().toString();
    startNode = cmbStartState.getSelectedItem().toString();
    endNode = cmbEndState.getSelectedItem().toString();
  }

  /**
   * Return name of component type.
   */

  public String componentType(String componentType) {
    if (componentType.equals("#S")) {
      componentType = "State Realisation";
    } else if (componentType.equals("#E")) {
      componentType = "Event";
    } else if (componentType.equals("#EI")) {
      componentType = "External Input";
    } else if (componentType.equals("#EO")) {
      componentType = "External Output";
    } else if (componentType.equals("#L")) {
      componentType = "Selection";
    } else if (componentType.equals("#G")) {
      componentType = "Guard";
    } else if (componentType.equals("#II")) {
      componentType = "Internal Input Event";
    } else if (componentType.equals("#IO")) {
      componentType = "Internal Output Event";
    } else if (componentType.equals("#A")) {
      componentType = "Assertion";
    }
    return componentType;
  }


  // /**
  // * Populate configuration for NOI.
  // */
  //
  // public void populateNOI() {
  // try {
  // if (nodesOfInterest.get(noiComboBox.getSelectedItem().toString()) != null) {
  // if (nodesOfInterest.get(noiComboBox.getSelectedItem().toString()) == true) {
  // chckbxNewCheckBox_1.setSelected(true);
  // } else {
  // chckbxNewCheckBox_1.setSelected(false);
  // }
  // } else {
  // chckbxNewCheckBox_1.setSelected(false);
  // }
  // } catch (Exception e) {
  //
  // }
  // }


  /**
   * Populate configuration for TSS.
   */

  public void updateTSS() {
    try {
      String selectedTSS =
          cmbComponent.getSelectedItem().toString() + ";"
              + cmbBehaviour.getSelectedItem().toString();

      if (chosenTSS.get(selectedTSS) != null) {
        if (chosenTSS.get(selectedTSS) == true) {
          chkTSS.setSelected(true);
        } else {
          chkTSS.setSelected(false);
        }
      } else {
        chkTSS.setSelected(false);
      }
    } catch (Exception e) {

    }
  }


  /**
   * Populate configuration for Observable Responses.
   */

  public void populateOR() {
    try {
      String selectedOR =
          cmbObsComp.getSelectedItem().toString() + ";"
              + cmbObsBehaviour.getSelectedItem().toString();
      if (observableResponses.get(selectedOR) != null) {
        txtObs.setText(observableResponses.get(selectedOR));
      } else {
        txtObs.setText("");
      }
    } catch (Exception e) {

    }
  }


  /**
   * Populate configuration for User Actions.
   */

  public void populateUA() {
    try {
      String selectedUA =
          cmbUAComp.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString();
      if (userActions.get(selectedUA) != null) {
        String[] userAction = userActions.get(selectedUA);
        txtUA.setText(userAction[0]);

        if (userAction[1].equals("true")) {
          chckbxAppearPreAmble.setSelected(true);
        } else {
          chckbxAppearPreAmble.setSelected(false);
        }
      } else {
        txtUA.setText("");
        chckbxAppearPreAmble.setSelected(false);
      }
    } catch (Exception e) {

    }
  }


  /**
   * Populate test case configuration in TCC file format.
   */

  public void SaveConfig(boolean shutdown) {
    JFrame parentFrame = new JFrame();

    JFileChooser fileChooser = new JFileChooser();

    javax.swing.filechooser.FileFilter filter =
        new FileNameExtensionFilter("Test Case Config file (.TCC)", new String[] {"TCC"});
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);

    fileChooser.setDialogTitle("Specify a file to save");

    int userSelection = fileChooser.showSaveDialog(parentFrame);

    if (userSelection == JFileChooser.APPROVE_OPTION) {

      setRange();

      File fileToSave = fileChooser.getSelectedFile();
      System.out.println("Save as file: " + fileToSave.getAbsolutePath() + ".tcc");

      Object[] arr = new Object[18];

      arr[0] = componentList;
      arr[1] = AllTSSComponents;
      arr[2] = AllORComponents;
      arr[3] = AllUAComponents;
      arr[4] = nodesOfInterest;
      arr[5] = chosenTSS;
      arr[6] = observableResponses;
      arr[7] = userActions;
      arr[8] = BTArray;
      arr[9] = range;
      arr[10] = mapToTag;
      arr[11] = mapToTagName;
      arr[12] = mapToTag2;
      arr[13] = listOfTestPaths;
      arr[14] = testPaths;
      arr[15] = filenameStr;
      arr[16] = rangeNum;
      arr[17] = tagToIndexMap;

      FileOutputStream fos;
      try {
        fos = new FileOutputStream(fileToSave.getAbsolutePath() + ".tcc");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(arr);
        oos.close();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      frame.setTitle("TCGen-UI - Saved as " + fileChooser.getSelectedFile().getName() + ".tcc");

      if (shutdown == true)
        System.exit(0);
    }
  }

  public Main() {
    setResizable(false);
    setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Soh Wei Yu\\Documents\\icon.png"));
    setTitle("TP-Optimizer");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(222, -51, 1158, 850);
    setLocationRelativeTo(null);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);

    JPanel panel = new JPanel();
    panel.setBounds(63, 5, 0, 900);
    panel.setLayout(new GridLayout(0, 1, 0, 0));

    JButton btnSelectTestPaths = new JButton("Select TCP for Deletion");
    btnSelectTestPaths.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        TestPathsog c = new TestPathsog(frame, listOfTestPaths);
        c.showCentered();
      }
    });
    btnSelectTestPaths.setEnabled(false);
    btnSelectTestPaths.setBounds(301, 59, 235, 51);
    contentPane.add(btnSelectTestPaths);

    JButton btnGenerateTestCases = new JButton("Generate Test Cases");

    /**
     * Generate natural language test cases in Excel format.
     */
    btnGenerateTestCases.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          WritableWorkbook workbook = Workbook.createWorkbook(new File("test-cases//output.xls"));

          WritableSheet sheet = workbook.createSheet("Generated Test Case", 0);

          workbook.setColourRGB(Colour.LIGHT_TURQUOISE2, 208, 234, 255);

          WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
          WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

          headerFormat.setAlignment(jxl.format.Alignment.CENTRE);
          headerFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
          headerFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.MEDIUM,
              jxl.format.Colour.BLACK);
          headerFormat.setBackground(Colour.LIGHT_TURQUOISE2);
          headerFormat.setWrap(true);


          WritableCellFormat cellFormat2 = new WritableCellFormat();

          cellFormat2.setVerticalAlignment(VerticalAlignment.TOP);

          cellFormat2.setWrap(true);

          sheet.mergeCells(0, 0, 8, 0);


          String blocks = "";

          boolean isFirst7 = true;

          for (Entry<String, Boolean> entry : chosenTSS.entrySet()) {

            // generate list of chosen TSS to display on test case

            String key = entry.getKey();
            Boolean value = entry.getValue();


            if (value == true) {
              String[] parts = key.split(";");

              for (String[] nodeString : BTArray) {
                if (nodeString[1].equals("#S") && nodeString[2].equals(parts[0])
                    && nodeString[3].equals(parts[1])) {

                  String nodeFlag = "";
                  if (nodeString[4] != null) {
                    nodeFlag = nodeString[4];
                  }

                  if (isFirst7 == false) {
                    blocks += ", ";
                  }

                  String componentType = nodeString[1];
                  componentType = componentType(componentType);

                  blocks +=
                      nodeString[0] + ": " + nodeString[2] + " - " + nodeString[3] + " " + nodeFlag
                          + " [" + componentType + "]";
                  isFirst7 = false;


                }
              }

            }
          }

          Date date = new Date();
          // Label label11 =
          // new Label(0, 0, "Date & Time of Generation: " + date.toString()
          // + "\nGenerated from: " + filenameStr + "\nInitial State of the System: "
          // + range[0] + "\nTarget System States: " + taTSS.getText().replace("\n", "")
          // + "\nNodes of Interest: " + taNOI.getText().replace("\n", "")
          // + "\nObservable System Responses configured for: "
          // + taObservableResponses.getText().replace("\n", "")
          // + "\nUser Actions and External Inputs configured for: "
          // + taUserActions.getText().replace("\n", ""), cellFormat2);
          // sheet.addCell(label11);

          sheet.setRowView(0, 3000);

          Label label = new Label(0, 1, "Test Case ID", headerFormat);
          sheet.addCell(label);
          Label label9 = new Label(1, 1, "Start & End States", headerFormat);
          sheet.addCell(label9);
          Label label4 = new Label(2, 1, "Pre-amble", headerFormat);
          sheet.addCell(label4);
          Label label2 = new Label(3, 1, "User Actions", headerFormat);
          sheet.addCell(label2);
          Label label3 = new Label(4, 1, "Expected Observable Response", headerFormat);
          sheet.addCell(label3);
          Label label5 = new Label(5, 1, "Post-amble", headerFormat);
          sheet.addCell(label5);
          Label label6 = new Label(6, 1, "Nodes of Interest", headerFormat);
          sheet.addCell(label6);
          Label label7 = new Label(7, 1, "Pass/Fail", headerFormat);
          sheet.addCell(label7);
          Label label8 = new Label(8, 1, "Remarks", headerFormat);
          sheet.addCell(label8);

          sheet.setColumnView(0, 5);
          sheet.setColumnView(1, 10);
          sheet.setColumnView(2, 20);
          sheet.setColumnView(3, 20);
          sheet.setColumnView(4, 20);
          sheet.setColumnView(5, 20);
          sheet.setColumnView(6, 10);
          sheet.setColumnView(7, 5);
          sheet.setColumnView(8, 18);

          int pathNum = 2;
          int testCaseID = 1;
          int testCaseStartingCell = 2;

          WritableCellFormat cellFormat = new WritableCellFormat();
          cellFormat.setWrap(true);
          cellFormat.setVerticalAlignment(VerticalAlignment.TOP);

          cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
          cellFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);


          cellFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.MEDIUM,
              jxl.format.Colour.BLACK);

          for (ArrayList<ArrayList<Element>> pathAndPA : testPaths.values()) {

            // start generating pre-amble, test paths and post-ambls

            ArrayList<Element> path = pathAndPA.get(1);

            Label labelNum = new Label(0, pathNum, Integer.toString(testCaseID), cellFormat);
            sheet.addCell(labelNum);
            testCaseStartingCell = pathNum;

            String userActionForPath = "";
            String observableResponseForPath = "";


            String last = "";

            Boolean isFirst = true;
            Boolean isFirst2 = true;

            int userAction = 1;

            String pathNOI = "";
            Boolean isFirstNOI = true;

            for (Element block : path) {
              ArrayList<NodeData> listOfTags = mapToTag2.get(block.getText());


              for (NodeData node : listOfTags) {
                // nodes = nodes of the test path

                for (Entry<String, Boolean> entry : nodesOfInterest.entrySet()) {
                  String key = entry.getKey();
                  Boolean value = entry.getValue();

                  if (value == true) {
                    String[] parts = key.split(":");
                    if (parts[0].substring(5).equals(node.getTag())) {

                      if (isFirstNOI == false) {
                        pathNOI += ", ";
                      }
                      pathNOI += "Node " + node.getTag();

                      isFirstNOI = false;
                    }
                  }
                  // do what you have to do here
                  // In your case, an other loop.
                }


                for (String[] nodeString : BTArray) {
                  String[] userAction2 =
                      userActions.get(nodeString[2] + ";" + nodeString[3] + " ["
                          + componentType(nodeString[1]) + "]");

                  if (nodeString[0].substring(5).equals(node)) {

                    if (observableResponses.get(nodeString[2] + ";" + nodeString[3] + " ["
                        + componentType(nodeString[1]) + "]") != null) {
                      // if node is an observable response, do the following

                      if (isFirst2 == false) {
                        observableResponseForPath += "\n";
                      }

                      observableResponseForPath +=
                          observableResponses.get(nodeString[2] + ";" + nodeString[3] + " ["
                              + componentType(nodeString[1]) + "]")
                              + " [Node " + node + "]";

                      isFirst2 = false;

                      if (last.equals("UA") || last.equals("UA-OR")) {
                        // expected observable should be placed next to prior user action
                        last = "UA-OR";
                      } else {
                        last = "OR";
                      }

                    } else if (!(userAction2 == null) && !userAction2[0].equals("")
                        && !userAction2[1].equals("true")) {
                      if (last.equals("OR")) {
                        Label labelObservableResponse =
                            new Label(4, pathNum, observableResponseForPath, cellFormat);
                        sheet.addCell(labelObservableResponse);
                        observableResponseForPath = "";

                        userActionForPath = "";
                        isFirst = true;
                        isFirst2 = true;

                      } else if (last.equals("UA-OR")) {

                        Label labelObservableResponse =
                            new Label(4, pathNum, observableResponseForPath, cellFormat);
                        sheet.addCell(labelObservableResponse);
                        observableResponseForPath = "";

                        userActionForPath = "";
                        isFirst = true;
                        isFirst2 = true;
                      }

                      if (!last.equals("UA") && !last.equals("")) {
                        pathNum++;
                      }

                      if (!userActionForPath.equals("")) {
                        userActionForPath += "\n";
                      }

                      userActionForPath +=
                          userAction + ") " + userAction2[0] + " [Node " + node + "]";
                      isFirst = false;

                      Label labelUserActions = new Label(3, pathNum, userActionForPath, cellFormat);
                      sheet.addCell(labelUserActions);


                      Label passFail = new Label(7, pathNum, "", cellFormat);
                      sheet.addCell(passFail);

                      Label remarks = new Label(8, pathNum, "", cellFormat);
                      sheet.addCell(remarks);

                      userAction++;

                      last = "UA";


                    }


                  }

                }
              }

            }

            Label nodeNOI = new Label(6, testCaseStartingCell, pathNOI, cellFormat);
            sheet.addCell(nodeNOI);


            if (last.equals("OR")) {

              Label labelObservableResponse =
                  new Label(4, pathNum, observableResponseForPath, cellFormat);
              sheet.addCell(labelObservableResponse);

              Label passFail = new Label(6, pathNum, "", cellFormat);
              sheet.addCell(passFail);

              Label remarks = new Label(7, pathNum, "", cellFormat);
              sheet.addCell(remarks);

              if (userActionForPath.equals("")) {
                Label labelUserActions = new Label(3, pathNum, "", cellFormat);
                sheet.addCell(labelUserActions);
              }
            } else if (last.equals("UA-OR")) {
              Label labelObservableResponse =
                  new Label(4, pathNum, observableResponseForPath, cellFormat);
              sheet.addCell(labelObservableResponse);

              Label passFail = new Label(6, pathNum, "", cellFormat);
              sheet.addCell(passFail);

              Label remarks = new Label(7, pathNum, "", cellFormat);
              sheet.addCell(remarks);

              if (userActionForPath.equals("")) {
                Label labelUserActions = new Label(3, pathNum, "", cellFormat);
                sheet.addCell(labelUserActions);
              }
            } else if (last.equals("UA")) {
              Label labelObservableResponse = new Label(4, pathNum, "", cellFormat);
              sheet.addCell(labelObservableResponse);
            } else if (last.equals("")) {

              Label labelUserActions = new Label(3, pathNum, "", cellFormat);
              sheet.addCell(labelUserActions);
              Label labelObservableResponse = new Label(4, pathNum, "", cellFormat);
              sheet.addCell(labelObservableResponse);

            }


            sheet.mergeCells(0, testCaseStartingCell, 0, pathNum);
            Label testCaseNum = new Label(0, pathNum, Integer.toString(testCaseID), cellFormat);
            sheet.addCell(testCaseNum);

            sheet.mergeCells(1, testCaseStartingCell, 1, pathNum);
            sheet.mergeCells(2, testCaseStartingCell, 2, pathNum);
            sheet.mergeCells(5, testCaseStartingCell, 5, pathNum);
            sheet.mergeCells(6, testCaseStartingCell, 6, pathNum);
            sheet.mergeCells(7, testCaseStartingCell, 7, pathNum);
            sheet.mergeCells(8, testCaseStartingCell, 8, pathNum);



            int preAmbleStepNum = 1;
            String preAmbleStr = "";
            boolean isFirst3 = true;

            // begin building preAmble, first starting with user actions within test path that has
            // "true" value for being a preamble

            for (Element block : path) {
              // nodes = nodes of the test path
              ArrayList<NodeData> listOfNodes = mapToTag2.get(block.getText());
              for (NodeData node : listOfNodes) {

                for (String[] nodeString : BTArray) {
                  String[] userAction4 =
                      userActions.get(nodeString[2] + ";" + nodeString[3] + " ["
                          + componentType(nodeString[1]) + "]");

                  if (nodeString[0].substring(5).equals(node.getTag())) {

                    if (!(userAction4 == null) && !userAction4[0].equals("")
                        && !userAction4[1].equals("false")) {
                      if (isFirst3 == false) {
                        preAmbleStr += "\n";
                      }

                      preAmbleStr +=
                          preAmbleStepNum + ") " + userAction4[0] + " [Node " + node.getTag() + "]";


                      isFirst3 = false;
                      preAmbleStepNum++;
                    }


                  }
                }
              }
            }

            // add to pre-amble by parsing nodes from postAmble that needs to be placed into
            // pre-amble

            ArrayList<Element> postAmble = pathAndPA.get(2);

            if (postAmble != null) {
              for (Element block : postAmble) {
                // nodes = nodes of the test path

                ArrayList<NodeData> listOfNodes = mapToTag2.get(block.getText());

                for (NodeData node : listOfNodes) {

                  for (String[] nodeString : BTArray) {
                    String[] userAction6 =
                        userActions.get(nodeString[2] + ";" + nodeString[3] + " ["
                            + componentType(nodeString[1]) + "]");


                    if (nodeString[0].substring(5).equals(node.getTag())) {

                      if (!(userAction6 == null) && !userAction6[0].equals("")
                          && !userAction6[1].equals("false")) {
                        if (isFirst3 == false) {
                          preAmbleStr += "\n";
                        }

                        preAmbleStr +=
                            preAmbleStepNum + ") " + userAction6[0] + " [Node " + node.getTag()
                                + "]";


                        isFirst3 = false;
                        preAmbleStepNum++;
                      }


                    }
                  }
                }
              }
            }

            // add the main elements in preAmble

            ArrayList<Element> preAmble = pathAndPA.get(0);

            if (preAmble != null) {
              for (Element block : preAmble) {
                ArrayList<NodeData> listOfNodes = mapToTag2.get(block.getText());

                for (NodeData node : listOfNodes) {
                  for (String[] nodeString : BTArray) {

                    String[] userAction3 =
                        userActions.get(nodeString[2] + ";" + nodeString[3] + " ["
                            + componentType(nodeString[1]) + "]");

                    if (nodeString[0].substring(5).equals(node.getTag())) {
                      if (!(userAction3 == null) && !userAction3[0].equals("")
                          && !userAction3[1].equals("true")) {
                        if (isFirst3 == false) {
                          preAmbleStr += "\n";
                        }

                        preAmbleStr +=
                            preAmbleStepNum + ") " + userAction3[0] + " [Node " + node.getTag()
                                + "]";


                        isFirst3 = false;
                        preAmbleStepNum++;
                      }
                    }
                  }

                }
              }
            } else {
              preAmbleStr = "No pre-amble generated.";
            }

            if (preAmbleStr.equals("")) {
              preAmbleStr = "No pre-amble generated.";
            }
            Label preAmble2 = new Label(2, testCaseStartingCell, preAmbleStr, cellFormat);
            sheet.addCell(preAmble2);


            // generate the post-amble

            int postAmbleStepNum = 1;
            String postAmbleStr = "";
            boolean isFirst4 = true;

            if (postAmble != null) {
              for (Element block : postAmble) {
                ArrayList<NodeData> listOfNodes = mapToTag2.get(block.getText());
                for (NodeData node : listOfNodes) {
                  for (String[] nodeString : BTArray) {
                    if (nodeString[0].substring(5).equals(node.getTag())) {

                      String[] userAction5 =
                          userActions.get(nodeString[2] + ";" + nodeString[3] + " ["
                              + componentType(nodeString[1]) + "]");

                      if (!(userAction5 == null) && !userAction5[0].equals("")
                          && !userAction5[1].equals("true")) {

                        if (isFirst4 == false) {
                          postAmbleStr += "\n";
                        }

                        postAmbleStr +=
                            postAmbleStepNum + ") " + userAction5[0] + " [Node " + node.getTag()
                                + "]";


                        isFirst4 = false;
                        postAmbleStepNum++;
                      }
                    }
                  }
                }
              }
            } else {
              postAmbleStr = "No post-amble generated.";
            }

            Label postAmbleLbl = new Label(5, testCaseStartingCell, postAmbleStr, cellFormat);
            sheet.addCell(postAmbleLbl);


            Element firstTag = path.get(0);
            Element lastTag = path.get(path.size() - 1);
            ArrayList<NodeData> firstTagArr = mapToTag2.get(firstTag.getText());
            ArrayList<NodeData> lastTagArr = mapToTag2.get(lastTag.getText());
            Label labelFirstLastTag =
                new Label(1, testCaseStartingCell, "Node " + firstTagArr.get(0).getTag()
                    + " to Node " + lastTagArr.get(0).getTag(), cellFormat);
            sheet.addCell(labelFirstLastTag);



            Label passFailLbl = new Label(7, testCaseStartingCell, "", cellFormat);
            sheet.addCell(passFailLbl);


            Label remarksLbl = new Label(8, testCaseStartingCell, "", cellFormat);
            sheet.addCell(remarksLbl);

            testCaseID++;
            pathNum++;
          }

          SheetSettings setting = sheet.getSettings();

          setting.setOrientation(PageOrientation.LANDSCAPE);

          workbook.write();
          workbook.close();


          if (Desktop.isDesktopSupported()) {
            Desktop dt = Desktop.getDesktop();
            dt.open(new File("test-cases//output.xls"));
          } else {
            openFolder();
          }

        } catch (Exception e) {
          // TODO Auto-generated catch block
          JOptionPane.showMessageDialog(null, "Error writing test case to " + e.getMessage());
          e.printStackTrace();
        }

      }
    });
    btnGenerateTestCases.setEnabled(false);
    btnGenerateTestCases.setBounds(546, 59, 281, 51);
    contentPane.add(btnGenerateTestCases);

    btnGenerateTestPaths = new JButton("Generate Test Case Paths");
    btnGenerateTestPaths.setEnabled(false);
    btnGenerateTestPaths.setBounds(10, 59, 281, 51);

    /**
     * Call API and generate TCPs, pre-amble and post-amble.
     */
    btnGenerateTestPaths.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ArrayList<NodeData> noiSelected = new ArrayList<NodeData>();
        int count = noiTable.getRowCount();
        for (int i = 0; i < count; i++) {
          if ((boolean) noiTable.getValueAt(count, 4)) {
            noiSelected.add(tagToNodeDataMap.get(noiTable.getValueAt(count, 0)));
          }
        }
        // TODO
        // intermediates is a list of block indices for the "nodes of interest" and
        // blocks is a list of block indices for the "system states".

        if ((rdbtnGenerateAll.isSelected() == false
            && rdbtnGenerateComponentTypes.isSelected() == false && rdbtnIWantTo.isSelected() == false)
            || (cmbInitialState.getSelectedIndex() == -1)
            || (rdbtnGenerateAll.isSelected() == false && (cmbStartState.getSelectedIndex() == -1 || cmbEndState
                .getSelectedIndex() == -1))) {
          JOptionPane.showMessageDialog(null, "Please configure the Range tabbed panel first.");
        } else {
          String content = "";

          String blocks = " (";

          boolean isFirst = true;

          for (Entry<String, Boolean> entry : chosenTSS.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();


            if (value == true) {
              String[] parts = key.split(";");

              for (String[] nodeString : BTArray) {
                if (nodeString[1].equals("#S") && nodeString[2].equals(parts[0])
                    && nodeString[3].equals(parts[1])) {
                  if (isFirst == false) {
                    blocks += " ";
                  }
                  blocks += tagToIndexMap.get(nodeString[0].substring(5));
                  isFirst = false;
                }
              }

            }
          }

          blocks += ")";

          String intermediates = " (";

          boolean isFirst2 = true;

          for (Entry<String, Boolean> entry : nodesOfInterest.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();

            if (value == true) {
              String[] parts = key.split(":");
              if (isFirst2 == false) {
                intermediates += " ";
              }
              intermediates += tagToIndexMap.get(parts[0].substring(5));
              isFirst2 = false;
            }

          }
          intermediates += ") ";

          String initialStateNodeTag = range[0];
          String[] initialStateArr = initialStateNodeTag.split(":");
          initialStateNodeTag = initialStateArr[0].substring(5);
          Integer initialStateBlockIndex = tagToIndexMap.get(initialStateNodeTag);
          String[] startPoint0 = null;
          String[] endPoint0 = null;

          ArrayList<ArrayList<String>> startEndCombo = new ArrayList<ArrayList<String>>();

          if (rangeNum == 3) {
            if (range[0] != null && range[1] != null && range[2] != null) {
              startPoint0 = range[1].split(":");
              endPoint0 = range[2].split(":");

              ArrayList<String> startEnd = new ArrayList<String>();
              startEnd.add(startPoint0[0]);
              startEnd.add(endPoint0[0]);
              startEndCombo.add(startEnd);
            }
          } else if (rangeNum == 2) {

            range[1] = cmbStartState.getSelectedItem().toString();
            range[2] = cmbEndState.getSelectedItem().toString();


            startPoint0 = range[1].split(" - ");
            endPoint0 = range[2].split(" - ");



            for (String[] nodeString : BTArray) {

              String nodeFlag = "";

              if (nodeString[4] != null) {
                nodeFlag = nodeString[4];
              }

              if (nodeString[1].equals("#S") && nodeString[2].equals(startPoint0[0])
                  && nodeString[3].equals(startPoint0[1]) && !nodeFlag.equals("^")
                  && !nodeFlag.equals("=>")) {

                for (String[] nodeString2 : BTArray) {

                  if (nodeString2[1].equals("#S") && nodeString2[2].equals(endPoint0[0])
                      && nodeString2[3].equals(endPoint0[1])) {
                    ArrayList<String> startEnd = new ArrayList<String>();
                    startEnd.add(nodeString[0]);
                    startEnd.add(nodeString2[0]);
                    startEndCombo.add(startEnd);
                  }
                }
              }
            }
          } else if (rangeNum == 1) {

            for (String IP : comboBoxstrNonReversion) {
              startPoint0 = IP.split(":");
              for (String IP2 : comboBoxstr) {
                endPoint0 = IP2.split(":");

                ArrayList<String> startEnd = new ArrayList<String>();
                startEnd.add(startPoint0[0]);
                startEnd.add(endPoint0[0]);
                startEndCombo.add(startEnd);
              }
            }
          }
          // change initialpoint to arraylist of initialpoints

          int i7 = 0;

          listOfTestPaths = new LinkedHashMap<>();
          testPaths = new LinkedHashMap<String, ArrayList<ArrayList<Element>>>();


          for (ArrayList<String> startEnd : startEndCombo) {
            String startPoint = startEnd.get(0);
            String endPoint = startEnd.get(1);

            if (startPoint != null && endPoint != null) {
              ArrayList<Integer> possibleTargets = new ArrayList<Integer>();

              for (String[] nodeString : BTArray) {
                if (nodeString[0].equals("Node " + initialStateNodeTag)) {
                  possibleTargets.add(tagToIndexMap.get(nodeString[0].substring(5)));
                }
              }

              content =
                  "(find-test-paths " + tagToIndexMap.get(startPoint.substring(5)) + intermediates
                      + tagToIndexMap.get(endPoint.substring(5)) + blocks + ")";

              org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();

              org.jdom2.Document doc = null;
              try {
                doc = saxBuilder.build(new StringReader(sbcl.sendCommand(content)));
              } catch (JDOMException | IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }

              Element rootNode = doc.getRootElement();

              List<Element> list = rootNode.getChildren("path");


              List<Element> listOfBlockIndexes = null;


              Boolean noPostAmble = false;

              for (int i2 = 0; i2 < list.size(); i2++) {

                Element block = list.get(i2); // a test path

                listOfBlockIndexes = block.getChildren("block-index");

                String testPath = "";

                // build the command to generate pre-amble and post-amble
                String preAmbleCommand = "(test-path-preamble " + initialStateBlockIndex + " (";
                String checkFeasibleCommand = "(check-test-path (";

                String pathStr = "";
                for (int i3 = 0; i3 < listOfBlockIndexes.size(); i3++) {

                  if (!(listOfBlockIndexes == null)) {
                    Element node2 = listOfBlockIndexes.get(i3); // a block index

                    if (i3 > 0) {
                      pathStr += " ";
                    }

                    pathStr += node2.getText();

                    // for each test path add the array list

                    ArrayList<NodeData> listOfNodes = mapToTag2.get(node2.getText());
                    for (NodeData node : listOfNodes) {
                      for (String[] nodeString : BTArray) {
                        if (nodeString[0].substring(5).equals(node.getTag())) {
                          String nodeFlag = "";
                          if (nodeString[4] != null) {
                            nodeFlag = nodeString[4];
                          }
                          String componentType = nodeString[1];
                          componentType = componentType(componentType);
                          testPath +=
                              "\n" + nodeString[0] + ": " + nodeString[2] + " - " + nodeString[3]
                                  + " " + nodeFlag + " [" + componentType + "]";
                          break;
                        }
                      }
                    }
                  } else {
                    testPath = "[[no test paths could be generated]";
                  }
                }
                preAmbleCommand += pathStr + "))";

                checkFeasibleCommand += pathStr + "))";

                Element lastNode = listOfBlockIndexes.get(listOfBlockIndexes.size() - 1);

                for (NodeData node : mapToTag2.get(lastNode.getText())) {
                  for (String[] nodeString : BTArray) {
                    if (nodeString[0].substring(5).equals(node.getTag())) {
                      String nodeFlag = "";
                      if (nodeString[4] != null) {
                        nodeFlag = nodeString[4];
                      }

                      if (nodeFlag.equals("^")) {
                        for (String[] nodeString2 : BTArray) {
                          String chosenInitialState = "Node " + initialStateNodeTag;
                          if (nodeString2[0].equals(chosenInitialState)) {
                            if (nodeString[2].equals(nodeString2[2])
                                && nodeString[3].equals(nodeString2[3])) {
                              noPostAmble = true;
                            }
                          }
                        }
                      }
                    }
                  }
                }


                if (sbcl.sendCommand(checkFeasibleCommand).equals("<result>FEASIBLE</result>")) {

                  ArrayList<String> possiblePostAmbles = new ArrayList<String>();
                  for (Integer target : possibleTargets) {
                    String postAmbleCommand =
                        "(test-path-postamble " + target + " (" + pathStr + "))";
                    possiblePostAmbles.add(postAmbleCommand);
                  }

                  String preAmble = "";

                  org.jdom2.input.SAXBuilder saxBuilder4 = new SAXBuilder();

                  org.jdom2.Document doc4 = null;
                  try {
                    doc4 = saxBuilder4.build(new StringReader(sbcl.sendCommand(preAmbleCommand)));
                  } catch (JDOMException | IOException e) {
                    e.printStackTrace();
                  }

                  Element rootNode4 = doc4.getRootElement();

                  // go through each node of preamble and convert it into text

                  List<Element> listPreAmbleNodes = null;
                  Element list4 = (Element) rootNode4.getChild("path");

                  if (!(list4 == null)) {

                    listPreAmbleNodes = list4.getChildren("block-index");

                    for (int i4 = 0; i4 < listPreAmbleNodes.size(); i4++) {

                      Element node3 = listPreAmbleNodes.get(i4); // a post-amble node
                      // convert preamble node to text

                      ArrayList<NodeData> listOfNodes = mapToTag2.get(node3.getText());
                      for (NodeData node : listOfNodes) {
                        for (String[] nodeString : BTArray) {


                          if (nodeString[0].substring(5).equals(node.getTag())) {
                            String nodeFlag = "";
                            if (nodeString[4] != null) {
                              nodeFlag = nodeString[4];
                            }
                            String componentType = nodeString[1];
                            componentType = componentType(componentType);
                            preAmble +=
                                "\n" + nodeString[0] + ": " + nodeString[2] + " - " + nodeString[3]
                                    + " " + nodeFlag + " [" + componentType + "]";
                            break;
                          }
                        }

                      }
                    }


                  } else {
                    preAmble = "[[no pre-amble could be generated]";
                  }
                  ArrayList<ArrayList<Element>> path = new ArrayList<ArrayList<Element>>();
                  ArrayList<Element> listPreAmbleNodesAL = null;
                  ArrayList<Element> listOfBlockIndexesAL = null;
                  ArrayList<Element> listPostAmbleNodesAL = null;

                  // need to convert list to arraylist because list is not serializable and cannot
                  // be saved to TCC file

                  if (listPreAmbleNodes != null) {
                    listPreAmbleNodesAL = new ArrayList<Element>(listPreAmbleNodes);
                  }
                  if (listOfBlockIndexes != null) {
                    listOfBlockIndexesAL = new ArrayList<Element>(listOfBlockIndexes);
                  }

                  path.add(listPreAmbleNodesAL); // pre-amble path
                  path.add(listOfBlockIndexesAL); // test path
                  path.add(listPostAmbleNodesAL); // post-amble path
                  testPaths.put(Integer.toString(i7 + 1), path);

                  String[] testPathStr = new String[3];
                  testPathStr[0] = preAmble;
                  testPathStr[1] = testPath;
                  listOfTestPaths.put(Integer.toString(i7 + 1), testPathStr);
                  i7++;
                }
              }

            } else {
              content += "(find-test-paths 0" + intermediates + "0" + blocks + ")";
            }

          }
          TestPathsog c = new TestPathsog(frame, listOfTestPaths);
          c.showCentered();

          btnGenerateTestCases.setEnabled(true);
          btnSelectTestPaths.setEnabled(true);

          sbcl.sendCommand("(close-connection)");
          try {
            sbcl.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

    });
    contentPane.setLayout(null);
    contentPane.add(panel);
    contentPane.add(btnGenerateTestPaths);

    JButton btnOpenTestCase = new JButton("Open Test Case Folder");
    btnOpenTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openFolder();
      }

    });
    btnOpenTestCase.setBounds(837, 59, 301, 51);
    contentPane.add(btnOpenTestCase);

    JMenuBar menuBar = new JMenuBar();
    menuBar.setBounds(0, 0, 185, 40);
    contentPane.add(menuBar);

    JMenu mnFile = new JMenu("File");
    menuBar.add(mnFile);
    mnFile.setMnemonic(KeyEvent.VK_F);

    /**
     * Load BT File.
     */
    JMenuItem mntmLoadBtFile = new JMenuItem("Load BT File");
    mntmLoadBtFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JFrame OpenFile = new JFrame("FrameDemo");
        OpenFile.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        OpenFile.pack();

        FileDialog fd = new FileDialog(OpenFile, "Choose a BT file", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            if (name.substring(name.lastIndexOf('.') + 1).equals("bt")
                || name.substring(name.lastIndexOf('.') + 1).equals("btc")) {
              return true;
            }
            return false;
          }
        });
        fd.setDirectory(System.getProperty("user.home"));
        fd.setFile("*.bt;*.btc");
        fd.setVisible(true);
        String filename = fd.getFile();
        if (filename == null)
          System.out.println("You cancelled the choice");
        else {
          clearEverything();
          System.out.println("You chose " + fd.getDirectory() + filename);
          filenameStr = (fd.getDirectory() + filename).replace("\\", "/");

          connectToServer();

          // load bt-file into BTAnalyser
          System.out.println("Processing bt file");
          sbcl.sendCommand("(process-bt-file \"" + filenameStr + "\")");

          // important to ensure resulting TCPs are reachable/valid
          System.out.println("Ensure TCPs are reachable");
          sbcl.sendCommand("(reachable-states)");

          tagToIndexMap = new HashMap<>();

          System.out.println("Building BT");
          String result = sbcl.sendCommand("(print-bt)");
          if (!result.equals("<result><error>No Behavior Tree loaded.</error></result>")) {
            BTModelReader modelReader = new BTModelReader(result);
            indexToNodeMap = modelReader.getIndexToNodeMap();
            tagToIndexMap = modelReader.getTagToIndexMap();
            root = indexToNodeMap.get(0);
          } else {
            printErrorMessage("error|6|");
            return;
          }

          btnGenerateTestCases.setEnabled(false);
          btnSelectTestPaths.setEnabled(false);

          populateData();
          isLoaded = true;
          frame.setTitle("TP-Optimizer - " + filename);
        }
      }
    });
    mnFile.add(mntmLoadBtFile);


    /**
     * Load configuration file for TCGen-UI
     */
    JMenuItem mntmSave = new JMenuItem("Load Test Case Config");
    mntmSave.addActionListener(new ActionListener() {
      @SuppressWarnings("unchecked")
      public void actionPerformed(ActionEvent arg0) {

        // 1. Create the frame.
        JFrame frame2 = new JFrame("FrameDemo");

        // 2. Optional: What happens when the frame closes?
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 3. Create components and put them in the frame.
        // ...create emptyLabel...
        // frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

        // 4. Size the frame.
        frame2.pack();

        // 5. Show it.

        FileDialog fd =
            new FileDialog(frame2, "Choose a Test Case Config (TCC) file", FileDialog.LOAD);
        fd.setDirectory(System.getProperty("user.home"));
        fd.setFile("*.tcc");

        fd.setVisible(true);
        String filename = fd.getFile();
        if (filename == null)
          System.out.println("You cancelled the choice");
        else {
          System.out.println("You chose " + fd.getDirectory() + filename);

          clearEverything();

          FileInputStream fis;
          try {
            fis = new FileInputStream(fd.getDirectory() + filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object[] arr = (Object[]) ois.readObject();
            ois.close();

            componentList = (ArrayList<ArrayList<String[]>>) arr[0];
            nodesOfInterest = (HashMap<String, Boolean>) arr[4];
            chosenTSS = (HashMap<String, Boolean>) arr[5];
            observableResponses = (HashMap<String, String>) arr[6];
            userActions = (HashMap<String, String[]>) arr[7];
            BTArray = (ArrayList<String[]>) arr[8];
            range = (String[]) arr[9];
            mapToTag = (HashMap<String, String>) arr[10];
            mapToTagName = (HashMap<String, String>) arr[11];
            mapToTag2 = (HashMap<String, ArrayList<NodeData>>) arr[12];
            listOfTestPaths = (LinkedHashMap<String, String[]>) arr[13];
            testPaths = (LinkedHashMap<String, ArrayList<ArrayList<Element>>>) arr[14];
            filenameStr = (String) arr[15];
            rangeNum = (Integer) arr[16];
            tagToIndexMap = (HashMap<String, Integer>) arr[17];

            populateData();
            updateTSS();
            updateRangeTab();
            // populateNOI();
            populateOR();
            populateUA();
            isLoaded = true;

            frame.setTitle("TCGen-UI - Loaded from " + filename);


            bg.add(rdbtnGenerateAll);
            bg.add(rdbtnGenerateComponentTypes);
            bg.add(rdbtnIWantTo);

            if (rangeNum == 1) {
              rdbtnGenerateAll.setSelected(true);
            } else if (rangeNum == 2) {
              rdbtnGenerateComponentTypes.setSelected(true);
            } else if (rangeNum == 3) {
              rdbtnIWantTo.setSelected(true);
            } else {
              bg.clearSelection();
            }


            if (range[0] != null) {
              cmbInitialState.setSelectedItem(range[0]);
            }
            if (range[1] != null) {
              cmbStartState.setSelectedItem(range[1]);
            }
            if (range[2] != null) {
              cmbEndState.setSelectedItem(range[2]);
            }


          } catch (Exception e) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null,
                "Please make sure the config file is not corrupted. Error: " + e.getMessage());
            e.printStackTrace();
          }

          setRange();

          int total = 0;

          for (String key : listOfTestPaths.keySet()) {
            total++;
          }
          if (total > 0) {
            btnGenerateTestCases.setEnabled(true);
            btnSelectTestPaths.setEnabled(true);
          } else {
            btnGenerateTestCases.setEnabled(true);
            btnSelectTestPaths.setEnabled(true);
          }

        }
      }
    });
    mnFile.add(mntmSave);

    JCheckBoxMenuItem chckbxmntmSaveSetting = new JCheckBoxMenuItem("Save Test Case Config");
    chckbxmntmSaveSetting.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SaveConfig(false);
      }
    });
    mnFile.add(chckbxmntmSaveSetting);

    JMenuItem mntmExit = new JMenuItem("Exit");
    mntmExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (isLoaded == false) {
          System.exit(0);
        } else {
          int dialogButton = JOptionPane.YES_NO_OPTION;
          int dialogResult =
              JOptionPane.showConfirmDialog(null,
                  "Would you like to save your configurations first?", "Warning", dialogButton);
          if (dialogResult == JOptionPane.YES_OPTION) {
            SaveConfig(true);
          } else {
            System.exit(0);
          }
        }
      }
    });
    mnFile.add(mntmExit);

    JMenu mnHelp = new JMenu("Help");
    menuBar.add(mnHelp);

    JMenuItem mntmTips = new JMenuItem("Tips");
    mntmTips.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane
            .showMessageDialog(
                frame,
                "Instructions on generating test cases: \n\n1) File > Load BT > Choose BT file \n2) Fill in 'Nodes of Interest', 'Target System State', and 'Range' (you must select Target System State before Range) \n3) Click on Generate Test Paths \n4) Fill in Observable Responses and User Actions \n5) Click Generate Test Cases \n6) Save Config by File > Save Test Case Config\n\nDo note that you do not need to fill in 'Observable Responses' and 'User Actions' to generate test paths, but that is required for generating test cases.",
                "Tips", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    mnHelp.add(mntmTips);

    JMenuItem mntmAbout = new JMenuItem("About");
    mntmAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane
            .showMessageDialog(
                frame,
                "TP-Optimizer\r\nBy Mitchell Savell\r\nmitchellsavell@gmail.com\r\n\r\nBased on TCGen-UI\r\nBy Soh Wei Yu",
                "About", JOptionPane.PLAIN_MESSAGE);

      }
    });
    mnHelp.add(mntmAbout);

    JTabbedPane tabbedPane_3 = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane_3.setBounds(63, 152, 5, 5);
    contentPane.add(tabbedPane_3);

    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setEnabled(false);
    tabbedPane.setBounds(10, 131, 1131, 660);
    contentPane.add(tabbedPane);

    JPanel panel_2 = new JPanel();
    tabbedPane.addTab("Nodes of Interest", null, panel_2, null);
    panel_2.setLayout(null);
    lblSelectedNodes.setFont(new Font("Tahoma", Font.PLAIN, 18));

    lblSelectedNodes.setBounds(114, 29, 215, 40);
    panel_2.add(lblSelectedNodes);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(53, 80, 1031, 523);
    panel_2.add(scrollPane);

    noiTable = new JTable();
    noiTable.setBorder(null);
    scrollPane.setViewportView(noiTable);
    noiTable.setFillsViewportHeight(true);
    noiTable.setCellSelectionEnabled(false);
    noiTable.setRowSelectionAllowed(false);
    noiTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] {"Tag", "Component",
        "Behaviour", "Behaviour Type", "Select"}) {
      private static final long serialVersionUID = -8673519275754805408L;
      Class[] columnTypes = new Class[] {Object.class, Object.class, Object.class, Object.class,
          Boolean.class};

      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    noiTable.getColumnModel().getColumn(1).setResizable(false);
    noiTable.getColumnModel().getColumn(2).setResizable(false);
    noiTable.getColumnModel().getColumn(3).setResizable(false);
    noiTable.getColumnModel().getColumn(4).setResizable(false);

    JPanel panel_3 = new JPanel();
    tabbedPane.addTab("Target System States", null, panel_3, null);
    panel_3.setLayout(null);

    JLabel label = new JLabel("Select Component Name:");
    label.setBounds(122, 134, 281, 26);
    panel_3.add(label);

    JLabel label_1 = new JLabel("Select Behaviour:");
    label_1.setBounds(122, 181, 281, 26);
    panel_3.add(label_1);

    cmbComponent.setBounds(310, 131, 313, 32);
    panel_3.add(cmbComponent);

    cmbBehaviour.setBounds(310, 178, 313, 32);
    panel_3.add(cmbBehaviour);

    chkTSS.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        if (cmbComponent.getSelectedItem() != null && cmbBehaviour.getSelectedItem() != null) {
          String selectedTSS =
              cmbComponent.getSelectedItem().toString() + ";"
                  + cmbBehaviour.getSelectedItem().toString();

          if (chkTSS.isSelected()) {
            chosenTSS.put(selectedTSS, true);

          } else {
            chosenTSS.put(selectedTSS, false);
          }

          updateRangeTab();

          if (range[0] != null) {
            cmbInitialState.setSelectedItem(range[0]);
          }
          if (range[1] != null) {

            cmbStartState.setSelectedItem(range[1]);
          }
          if (range[2] != null) {
            cmbEndState.setSelectedItem(range[2]);
          }
        }
        updateTSSTA();
      }
    });
    chkTSS.setBounds(230, 254, 185, 35);
    panel_3.add(chkTSS);

    cmbComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent action) {
        System.out.println(action.getActionCommand());
        if (action.getActionCommand().equals("comboBoxChanged")) {
          updateTSSDisplay();
        }
      }
    });

    cmbBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updateTSS();
      }
    });


    JLabel lblNewLabel = new JLabel("Note: Selection restricted to state realisation nodes");
    lblNewLabel.setBounds(194, 296, 589, 26);
    panel_3.add(lblNewLabel);

    JLabel lblSelectedTargetSystem = new JLabel("Selected target system states:");
    lblSelectedTargetSystem.setBounds(743, 14, 603, 40);
    panel_3.add(lblSelectedTargetSystem);
    taTSS.setEditable(false);

    taTSS.setBounds(743, 60, 345, 562);
    panel_3.add(taTSS);


    JPanel panel_1 = new JPanel();
    tabbedPane.addTab("Range", null, panel_1, null);
    panel_1.setLayout(null);

    JLabel lblSelectFinalTss = new JLabel("Select End State:");
    lblSelectFinalTss.setBounds(187, 368, 281, 26);
    panel_1.add(lblSelectFinalTss);
    cmbStartState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRange();
        System.out.println("start node set to: " + startNode);
      }
    });

    cmbStartState.setBounds(478, 316, 313, 32);
    panel_1.add(cmbStartState);
    cmbEndState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setRange();
        System.out.println("final Node set to: " + endNode);
      }
    });

    cmbEndState.setBounds(478, 365, 313, 32);
    panel_1.add(cmbEndState);

    JLabel lblSelectInitialTss_1 = new JLabel("Select Start State:");
    lblSelectInitialTss_1.setBounds(187, 319, 281, 26);
    panel_1.add(lblSelectInitialTss_1);

    JLabel lblSelect = new JLabel("Select Initial State of the System:");
    lblSelect.setBounds(187, 52, 281, 26);
    panel_1.add(lblSelect);
    cmbInitialState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          range[0] = cmbInitialState.getSelectedItem().toString();
        } catch (Exception e) {

        }
      }
    });

    cmbInitialState.setBounds(482, 49, 446, 32);
    panel_1.add(cmbInitialState);

    JLabel lblNoteSelectionRestricted =
        new JLabel("Note: Selection restricted to Target System States which you have chosen.");
    lblNoteSelectionRestricted.setBounds(248, 432, 706, 26);
    panel_1.add(lblNoteSelectionRestricted);

    JLabel lblPleaseSelectYour = new JLabel("Please select your Target System States first.");
    lblPleaseSelectYour.setBounds(305, 466, 706, 26);
    panel_1.add(lblPleaseSelectYour);
    rdbtnGenerateAll.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        if (rdbtnGenerateAll.isSelected() == true) {
          lblSelectInitialTss_1.setVisible(false);
          lblSelectFinalTss.setVisible(false);
          cmbStartState.setVisible(false);
          cmbEndState.setVisible(false);
        }
      }
    });

    rdbtnGenerateAll.setBounds(187, 125, 706, 41);
    panel_1.add(rdbtnGenerateAll);
    bg.add(rdbtnGenerateAll);
    rdbtnGenerateComponentTypes.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (rdbtnGenerateComponentTypes.isSelected() == true) {
          updateRangeTabComponent();
          lblSelectInitialTss_1.setVisible(true);
          lblSelectFinalTss.setVisible(true);
          cmbStartState.setVisible(true);
          cmbEndState.setVisible(true);

          rangeNum = 2;


          if (range[0] != null) {
            cmbInitialState.setSelectedItem(range[0]);
          }
          if (range[1] != null) {

            cmbStartState.setSelectedItem(range[1]);
          }
          if (range[2] != null) {
            cmbEndState.setSelectedItem(range[2]);
          }
        }
      }
    });

    rdbtnGenerateComponentTypes.setBounds(187, 175, 683, 41);
    panel_1.add(rdbtnGenerateComponentTypes);
    bg.add(rdbtnGenerateComponentTypes);
    rdbtnIWantTo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (rdbtnIWantTo.isSelected() == true) {
          lblSelectInitialTss_1.setVisible(true);
          lblSelectFinalTss.setVisible(true);
          cmbStartState.setVisible(true);
          cmbEndState.setVisible(true);

          rangeNum = 3;

          updateRangeTab();

          if (range[0] != null) {
            cmbInitialState.setSelectedItem(range[0]);
          }
          if (range[1] != null) {
            cmbStartState.setSelectedItem(range[1]);
          }
          if (range[2] != null) {
            cmbEndState.setSelectedItem(range[2]);
          }
        }
      }
    });

    rdbtnIWantTo.setBounds(187, 229, 807, 41);
    panel_1.add(rdbtnIWantTo);
    bg.add(rdbtnIWantTo);

    JPanel panel_4 = new JPanel();
    tabbedPane.addTab("Observable Responses", null, panel_4, null);
    panel_4.setLayout(null);

    JLabel label_2 = new JLabel("Select Component Name:");
    label_2.setBounds(248, 21, 281, 26);
    panel_4.add(label_2);

    cmbObsComp.setBounds(524, 21, 313, 32);
    panel_4.add(cmbObsComp);

    cmbObsBehaviour.setBounds(524, 65, 313, 32);
    panel_4.add(cmbObsBehaviour);

    JLabel lblSelectBehavior = new JLabel("Select Behaviour:");
    lblSelectBehavior.setBounds(248, 68, 281, 26);
    panel_4.add(lblSelectBehavior);
    txtObs.setLineWrap(true);

    txtObs.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent arg0) {
        if (cmbObsComp.getSelectedItem() != null && cmbObsBehaviour.getSelectedItem() != null) {
          observableResponses.put(cmbObsComp.getSelectedItem().toString() + ";"
              + cmbObsBehaviour.getSelectedItem().toString(), txtObs.getText());
        }
        updateOTA();
      }
    });
    txtObs.setBounds(10, 261, 637, 289);
    panel_4.add(txtObs);


    cmbObsComp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updateObsBehaviours();
        populateOR();
      }
    });


    cmbObsBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        populateOR();
      }
    });


    JLabel label_4 = new JLabel("Observable Response:");
    label_4.setBounds(10, 205, 329, 26);
    panel_4.add(label_4);

    JLabel lblNoteSelectionRestricted_1 =
        new JLabel(
            "Note: Selection restricted to all components except events, external inputs, selections & guards.");
    lblNoteSelectionRestricted_1.setBounds(158, 127, 968, 42);
    panel_4.add(lblNoteSelectionRestricted_1);

    JLabel lblObservableResponsesEntered = new JLabel("Observable Responses configured for:");
    lblObservableResponsesEntered.setBounds(676, 197, 435, 42);
    panel_4.add(lblObservableResponsesEntered);


    taObservableResponses.setEditable(false);
    taObservableResponses.setBounds(676, 261, 405, 289);

    panel_4.add(taObservableResponses);

    JPanel panel_5 = new JPanel();
    tabbedPane.addTab("User Actions/External Inputs", null, panel_5, null);
    panel_5.setLayout(null);

    JLabel label_5 = new JLabel("Select Component Name:");
    label_5.setBounds(248, 21, 281, 26);
    panel_5.add(label_5);

    JLabel label_6 = new JLabel("Select Behaviour:");
    label_6.setBounds(248, 68, 281, 26);
    panel_5.add(label_6);

    cmbUAComp.setBounds(524, 21, 313, 32);
    panel_5.add(cmbUABehaviour);

    cmbUABehaviour.setBounds(524, 65, 313, 32);
    panel_5.add(cmbUAComp);


    JLabel lblUserActioninput = new JLabel("Action to be taken:");
    lblUserActioninput.setBounds(26, 163, 393, 26);
    panel_5.add(lblUserActioninput);
    txtUA.setLineWrap(true);

    txtUA.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (cmbUAComp.getSelectedItem() != null && cmbUABehaviour.getSelectedItem() != null
            && chckbxAppearPreAmble.isSelected() == true) {
          String[] userAction = new String[2];
          userAction[0] = txtUA.getText();
          userAction[1] = "true";
          userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString(), userAction);
        } else if (cmbUAComp.getSelectedItem() != null && cmbUABehaviour.getSelectedItem() != null
            && chckbxAppearPreAmble.isSelected() == false) {

          String[] userAction = new String[2];
          userAction[0] = txtUA.getText();
          userAction[1] = "false";
          userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString(), userAction);
        }
        updateUATA();
      }
    });
    txtUA.setBounds(10, 218, 637, 289);
    panel_5.add(txtUA);


    cmbUAComp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateUABehaviours();
        populateUA();
      }
    });


    cmbUABehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        populateUA();
      }
    });

    JLabel lblNoteSelectionRestricted_2 =
        new JLabel("Note: Selection restricted to events & external inputs.");
    lblNoteSelectionRestricted_2.setBounds(248, 122, 706, 26);
    panel_5.add(lblNoteSelectionRestricted_2);


    taUserActions.setEditable(false);
    taUserActions.setBounds(676, 218, 405, 289);
    panel_5.add(taUserActions);

    JLabel lblUserActionsConfigured = new JLabel("Actions configured for:");
    lblUserActionsConfigured.setBounds(676, 155, 435, 42);
    panel_5.add(lblUserActionsConfigured);
    chckbxAppearPreAmble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!txtUA.getText().equals("")) {
          if (chckbxAppearPreAmble.isSelected() == true) {
            String[] userAction = new String[2];
            userAction[0] = txtUA.getText();
            userAction[1] = "true";
            userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
                + cmbUABehaviour.getSelectedItem().toString(), userAction);
          } else {
            String[] userAction = new String[2];
            userAction[0] = txtUA.getText();
            userAction[1] = "false";
            userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
                + cmbUABehaviour.getSelectedItem().toString(), userAction);
          }
        }
      }
    });

    chckbxAppearPreAmble.setBounds(10, 519, 827, 35);
    panel_5.add(chckbxAppearPreAmble);


    JPanel panel_6 = new JPanel();

    List<TestPath> feeds = new ArrayList<TestPath>();
    feeds.add(new TestPath("ATM [READY]", "something", 5));
    feeds.add(new TestPath("Display [Transaction]", "something", 15));
    panel_6.setLayout(null);

    JTable table = new JTable(new TestPathModel(feeds));
    table.setDefaultRenderer(TestPath.class, new TestPathCell());
    table.setDefaultEditor(TestPath.class, new TestPathCell());
    table.setRowHeight(60);
    table.setBounds(731, 11, 500, 610);
    panel_6.add(table);

    tabbedPane.addTab("Join Test Paths", null, panel_6, null);
  }


  public static void printErrorMessage(String error) {
    System.out.println(error);
    int errorIndex =
        Integer.parseInt(error.substring(error.indexOf('|') + 1, error.lastIndexOf('|')));
    String errorMessage = error.substring(error.lastIndexOf('|') + 1);
    switch (errorIndex) {
      case 0:
        JOptionPane
            .showMessageDialog(null, "IP address name resolution failed.\r\n" + errorMessage);
        System.out.println();
        break;
      case 1:
        JOptionPane.showMessageDialog(null, "Unkown error while trying to connect to server.\r\n"
            + errorMessage);
        break;
      case 2:
        JOptionPane.showMessageDialog(null,
            "Message could not be sent. Connection may have been lost" + errorMessage);
        break;
      case 3:
        JOptionPane.showMessageDialog(null, "Unable to close lisp server.");
        break;
      case 4:
        JOptionPane.showMessageDialog(null, "Unable to start lisp server.");
        break;
      case 5:
        JOptionPane.showMessageDialog(null, "Communication with server timed out.");
        break;
      default:
        JOptionPane.showMessageDialog(null, "Other error.\r\n" + errorMessage);
        break;
    }
  }


  /**
   * Populate configuration in TCGen-UI
   */
  public void populateData() {

    // load nodes NOI table
    readNodes();

    populateTSSTab();
    populateObsTab();
    populateUATab();

    updateTSSTA();
    updateOTA();
    updateUATA();

    tabbedPane.setEnabled(true);
    btnGenerateTestPaths.setEnabled(true);
  }

  private void readNodes() {
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        tagToNodeDataMap.put(Integer.parseInt(node.getTag()), node);
        if (!compToBehaviourMap.containsKey(node.getComponent())) {
          compToBehaviourMap.put(node.getComponent(), new ArrayList<NodeData>());
        }
        compToBehaviourMap.get(node.getComponent()).add(node);
        Object[] rowData =
            {node.getTag(), node.getComponent(), node.getBehaviour(), node.getBehaviourType(), null};
        ((DefaultTableModel) noiTable.getModel()).addRow(rowData);
      }
    }
  }

  private void populateTSSTab() {
    Map<String, ArrayList<String>> tssComponents = new HashMap<String, ArrayList<String>>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getBehaviourType().equals("STATE-REALISATION")) {
          if (!tssComponents.containsKey(node.getComponent())) {
            tssComponents.put(node.getComponent(), new ArrayList<String>());
          }
          tssComponents.get(node.getComponent()).add(node.getBehaviour());
          chosenTSS.put(node.getComponent() + ";" + node.getBehaviour(), false);
        }
      }
    }

    String[] cmbData = tssComponents.keySet().toArray(new String[0]);
    cmbComponent.setModel(new DefaultComboBoxModel<String>(clean(cmbData)));
    updateTSSDisplay();
  }

  private void updateTSSDisplay() {
    Set<String> cmbBehaviourData = new HashSet<String>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getComponent().equals(cmbComponent.getItemAt(cmbComponent.getSelectedIndex()))
            && node.getBehaviourType().equals("STATE-REALISATION")) {
          cmbBehaviourData.add(node.getBehaviour());
        }
      }
    }
    cmbBehaviour.setModel(new DefaultComboBoxModel<String>(clean(cmbBehaviourData
        .toArray(new String[0]))));
    rdbtnGenerateAll.setSelected(true);
    updateTSS();
  }

  private void populateObsTab() {

    String[] obsComps = new String[compToBehaviourMap.size()];
    int i = 0;
    for (String key : compToBehaviourMap.keySet()) {
      boolean accepted = false;
      for (NodeData node : compToBehaviourMap.get(key)) {
        if (!Constants.unacceptedObsBehaviourTypes.contains(node.getBehaviourType())) {
          accepted = true;
          break;
        }
      }
      if (accepted) {
        obsComps[i++] = key;
      }
    }

    cmbObsComp.setModel(new DefaultComboBoxModel<String>(clean(obsComps)));
    updateObsBehaviours();
  }

  private void updateObsBehaviours() {
    String[] obsBehaviours =
        new String[compToBehaviourMap.get(cmbObsComp.getSelectedItem()).size()];
    int i = 0;
    if (cmbObsComp.getModel().getSize() > 0) {
      System.out.println(cmbObsComp.getSelectedItem());
      for (NodeData node : compToBehaviourMap.get(cmbObsComp.getSelectedItem())) {
        if (!Constants.unacceptedObsBehaviourTypes.contains(node.getBehaviourType())) {
          obsBehaviours[i++] = node.getBehaviour() + " [" + node.getBehaviourType() + "]";
        }
      }
    }
    cmbObsBehaviour.setModel(new DefaultComboBoxModel<String>(clean(obsBehaviours)));
  }

  private void populateUATab() {
    String[] uaComp = new String[compToBehaviourMap.size()];
    int i = 0;
    for (String key : compToBehaviourMap.keySet()) {
      boolean accepted = false;
      for (NodeData node : compToBehaviourMap.get(key)) {
        if (Constants.acceptedUABehaviourTypes.contains(node.getBehaviourType())) {
          accepted = true;
          break;
        }
      }
      if (accepted) {
        uaComp[i++] = key;
      }
    }

    cmbUAComp.setModel(new DefaultComboBoxModel<String>(clean(uaComp)));
    updateUABehaviours();
  }

  private void updateUABehaviours() {
    String[] uaBehaviours = new String[compToBehaviourMap.get(cmbUAComp.getSelectedItem()).size()];
    int i = 0;
    if (cmbUAComp.getModel().getSize() > 0) {
      for (NodeData node : compToBehaviourMap.get(cmbUAComp.getSelectedItem())) {
        if (Constants.acceptedUABehaviourTypes.contains(node.getBehaviourType())) {
          uaBehaviours[i++] = node.getBehaviour() + " [" + node.getBehaviourType() + "]";
        }
      }
    }
    cmbUABehaviour.setModel(new DefaultComboBoxModel<String>(clean(uaBehaviours)));
  }

  /**
   * Remove null elements.
   */
  public static String[] clean(final String[] v) {
    List<String> list = new ArrayList<String>(Arrays.asList(v));
    list.removeAll(Collections.singleton(null));
    return list.toArray(new String[list.size()]);
  }


  /**
   * Auto-fit columns in Excel sheet.
   */
  private void sheetAutoFitColumns(WritableSheet sheet) {
    for (int i = 0; i < sheet.getColumns(); i++) {
      Cell[] cells = sheet.getColumn(i);
      int longestStrLen = -1;

      if (cells.length == 0)
        continue;

      /* Find the widest cell in the column. */
      for (int j = 0; j < cells.length; j++) {
        if (cells[j].getContents().length() > longestStrLen) {
          String str = cells[j].getContents();
          if (str == null || str.isEmpty())
            continue;
          longestStrLen = str.trim().length();
        }
      }

      /* If not found, skip the column. */
      if (longestStrLen == -1)
        continue;

      /* If wider than the max width, crop width */
      if (longestStrLen > 255)
        longestStrLen = 255;

      CellView cv = sheet.getColumnView(i);
      cv.setSize(longestStrLen * 256 + 100); /* Every character is 256 units wide, so scale it. */
      sheet.setColumnView(i, cv);
    }
  }

  private void connectToServer() {
    System.out.println("Connecting to server");
    String connectionResult;
    try {
      connectionResult = sbcl.connect("localhost", 12);
      if (!connectionResult.equals("success")) {
        printErrorMessage(connectionResult);
        return;
      }
    } catch (InterruptedException | IOException e) {
      // nop
    }
    System.out.println("Successfully connected");
  }
}
