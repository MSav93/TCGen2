import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.jdom2.Element;

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
  private String filenameStr;

  LinkedHashMap<String, ArrayList<ArrayList<Element>>> testPaths =
      new LinkedHashMap<String, ArrayList<ArrayList<Element>>>();

  HashMap<String, ArrayList<NodeData>> mapToTag2 = new HashMap<>(); // map block index to tag
  public LinkedHashMap<String, String[]> listOfTestPaths = new LinkedHashMap<>();

  private ArrayList<String[]> BTArray = new ArrayList<String[]>();

  private JTable tblNOI;

  private JComboBox<String> cmbInitialState = new JComboBox<String>();
  private JComboBox<String> cmbTSSComponent = new JComboBox<String>();
  private JComboBox<String> cmbTSSBehaviour = new JComboBox<String>();
  private JComboBox<String> cmbORComponent = new JComboBox<String>();
  private JComboBox<String> cmbORBehaviour = new JComboBox<String>();
  private JComboBox<String> cmbUAComp = new JComboBox<String>();
  private JComboBox<String> cmbUABehaviour = new JComboBox<String>();



  private final JCheckBox chkTSS = new JCheckBox("This is a Target System State");
  private final JCheckBox chckbxAppearPreAmble =
      new JCheckBox(
          "This condition should appear in the Pre-Amble/This is an External Input not under user's control");
  private final JTextArea taORInput = new JTextArea();
  private final JTextArea txtUA = new JTextArea();
  private final JLabel lblSelectNodes = new JLabel("Select nodes of interest:");
  private final TextArea taTSS = new TextArea();
  private final TextArea taORConfigured = new TextArea();
  private final TextArea taUserActions = new TextArea();

  public static int instanceCount = 0;

  private int rangeNum = 0;

  private static Main frame = null;

  private Boolean isLoaded = false;

  private String[] comboBoxstr = new String[0];

  HashMap<String, Boolean> nodesOfInterest = new HashMap<>();
  HashMap<String, Boolean> chosenTSS = new HashMap<>();
  HashMap<String, String> observableResponses = new HashMap<>();
  HashMap<String, String[]> userActions = new HashMap<>();
  HashMap<String, String> mapToTag = new HashMap<>();
  HashMap<String, String> mapToTagName = new HashMap<>();
  HashMap<String, Integer> tagToIndexMap = new HashMap<>(); // map tag to block index

  private SBCLPipe sbcl = new SBCLPipe();
  private TreeMap<Integer, BTNode> indexToNodeMap = new TreeMap<Integer, BTNode>();
  private HashMap<Integer, NodeData> tagToNodeDataMap = new HashMap<Integer, NodeData>();
  private HashMap<String, ArrayList<NodeData>> compToBehaviourMap =
      new HashMap<String, ArrayList<NodeData>>();

  private String initialNode;

  private JTabbedPane tabbedPane;
  private JButton btnGenerateTestPaths;



  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e1) {
      System.err.println("System 'Look and Feel' could not be found. Using Windows 'Look and Feel'");
    }
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e1) {
      System.err.println("Windows 'Look and Feel' could not be found. Using default 'Look and Feel'");
    }
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
    taORConfigured.setText("");
    for (Entry<String, String> component : observableResponses.entrySet()) {
      String key = component.getKey();
      String value = component.getValue();

      if (!value.equals("")) {
        if (taORConfigured.getText().equals("")) {
          taORConfigured.setText(taORConfigured.getText() + key);
        } else {
          taORConfigured.setText(taORConfigured.getText() + ", \n" + key);
        }
      }
    }
    taORConfigured.setText(taORConfigured.getText().replaceAll(";", ": "));
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
    int count = tblNOI.getRowCount();
    for (int i = 0; i < count; i++) {
      ((DefaultTableModel) tblNOI.getModel()).removeRow(0);
    }
    cmbInitialState.removeAllItems();
    cmbTSSComponent.removeAllItems();
    cmbTSSBehaviour.removeAllItems();
    cmbORComponent.removeAllItems();
    cmbORBehaviour.removeAllItems();
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
    comboBoxstr = new String[0];

    chkTSS.setSelected(false);

    chckbxAppearPreAmble.setSelected(false);
    taORInput.setText("");
    txtUA.setText("");

    taTSS.setText("");

    taORConfigured.setText("");
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

  }


  /**
   * Open folder containing test cases.
   */
  public void openFolder() {
    String path =
        System.getProperty("user.dir") + System.getProperty("file.separator") + "test-cases";
    Path thePath = Paths.get(path);
    if (Files.exists(thePath)) {
      try {
        Desktop.getDesktop().open(new File(path));
      } catch (IOException e) {
        printErrorMessage("error|8");
        e.printStackTrace();
      }
    } else {
      boolean success = (new File(path)).mkdirs();
      if (!success) {
        // Directory creation failed
        printErrorMessage("error|7");
      } else {
        try {
          Desktop.getDesktop().open(
              new File(System.getProperty("user.dir") + System.getProperty("file.separator")
                  + "test-cases"));
        } catch (IOException e1) {
          printErrorMessage("error|8");
          e1.printStackTrace();
        }
      }
    }
  }

  /**
   * Record the selected option for range.
   */
  public void setRange() {
    initialNode = cmbInitialState.getSelectedItem().toString();
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
          cmbTSSComponent.getSelectedItem().toString() + ";"
              + cmbTSSBehaviour.getSelectedItem().toString();

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
          cmbORComponent.getSelectedItem().toString() + ";"
              + cmbORBehaviour.getSelectedItem().toString();
      if (observableResponses.get(selectedOR) != null) {
        taORInput.setText(observableResponses.get(selectedOR));
      } else {
        taORInput.setText("");
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

    JPanel panelMain = new JPanel();
    panelMain.setBounds(63, 5, 0, 900);
    panelMain.setLayout(new GridLayout(0, 1, 0, 0));

    JButton btnSelectTestPaths = new JButton("Select TCP for Deletion");
    btnSelectTestPaths.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        TestPathsog c = new TestPathsog(frame, listOfTestPaths);
        c.showCentered();
      }
    });
    btnSelectTestPaths.setEnabled(false);
    btnSelectTestPaths.setBounds(301, 59, 235, 51);
    btnSelectTestPaths.setEnabled(false);
    contentPane.add(btnSelectTestPaths);

    JButton btnGenerateTestCases = new JButton("Generate Test Cases");

    /**
     * Generate natural language test cases in Excel format.
     */
    btnGenerateTestCases.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // TODO Output to Excel
      }
    });
    btnGenerateTestCases.setEnabled(false);
    btnGenerateTestCases.setBounds(546, 59, 281, 51);
    btnGenerateTestCases.setEnabled(false);
    contentPane.add(btnGenerateTestCases);

    btnGenerateTestPaths = new JButton("Generate Test Case Paths");
    btnGenerateTestPaths.setEnabled(false);
    btnGenerateTestPaths.setBounds(10, 59, 281, 51);

    /**
     * Call API and generate TCPs, pre-amble and post-amble.
     */
    btnGenerateTestPaths.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {}

    });
    contentPane.setLayout(null);
    contentPane.add(panelMain);
    contentPane.add(btnGenerateTestPaths);

    JButton btnOpenTestCase = new JButton("Open Test Case Folder");
    btnOpenTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openFolder();
      }
    });
    btnOpenTestCase.setBounds(837, 59, 301, 51);
    contentPane.add(btnOpenTestCase);

    contentPane.add(createMenuBar());

    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBounds(10, 131, 1131, 660);
    contentPane.add(tabbedPane);

    JPanel panelNOI = new JPanel();
    tabbedPane.addTab("Nodes of Interest", null, panelNOI, null);
    panelNOI.setLayout(null);
    lblSelectNodes.setFont(new Font("Tahoma", Font.PLAIN, 18));

    lblSelectNodes.setBounds(53, 47, 191, 22);
    panelNOI.add(lblSelectNodes);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(53, 80, 1031, 523);
    panelNOI.add(scrollPane);

    tblNOI = new JTable();
    tblNOI.setBorder(null);
    scrollPane.setViewportView(tblNOI);
    tblNOI.setFillsViewportHeight(true);
    tblNOI.setCellSelectionEnabled(false);
    tblNOI.setRowSelectionAllowed(false);
    tblNOI.setModel(new DefaultTableModel(new Object[][] {}, new String[] {"Tag", "Component",
        "Behaviour", "Behaviour Type", "Select"}) {
      private static final long serialVersionUID = -8673519275754805408L;
      @SuppressWarnings("rawtypes")
      Class[] columnTypes = new Class[] {Object.class, Object.class, Object.class, Object.class,
          Boolean.class};

      @SuppressWarnings({"unchecked", "rawtypes"})
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    tblNOI.getColumnModel().getColumn(1).setResizable(false);
    tblNOI.getColumnModel().getColumn(2).setResizable(false);
    tblNOI.getColumnModel().getColumn(3).setResizable(false);
    tblNOI.getColumnModel().getColumn(4).setResizable(false);

    JPanel panelTSS = new JPanel();
    tabbedPane.addTab("Target System States", null, panelTSS, null);
    panelTSS.setLayout(null);

    JLabel lblTSSComponent = new JLabel("Select Component Name:");
    lblTSSComponent.setBounds(125, 136, 121, 14);
    panelTSS.add(lblTSSComponent);

    JLabel lblTSSBehaviour = new JLabel("Select Behaviour:");
    lblTSSBehaviour.setBounds(162, 183, 84, 14);
    panelTSS.add(lblTSSBehaviour);

    cmbTSSComponent.setBounds(256, 127, 313, 32);
    panelTSS.add(cmbTSSComponent);

    cmbTSSBehaviour.setBounds(256, 174, 313, 32);
    panelTSS.add(cmbTSSBehaviour);

    chkTSS.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        if (cmbTSSComponent.getSelectedItem() != null && cmbTSSBehaviour.getSelectedItem() != null) {
          String selectedTSS =
              cmbTSSComponent.getSelectedItem().toString() + ";"
                  + cmbTSSBehaviour.getSelectedItem().toString();

          if (chkTSS.isSelected()) {
            chosenTSS.put(selectedTSS, true);

          } else {
            chosenTSS.put(selectedTSS, false);
          }

          updateRangeTab();
        }
        updateTSSTA();
      }
    });
    chkTSS.setBounds(256, 224, 165, 23);
    panelTSS.add(chkTSS);

    cmbTSSComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent action) {
        System.out.println(action.getActionCommand());
        if (action.getActionCommand().equals("comboBoxChanged")) {
          updateTSSDisplay();
        }
      }
    });

    cmbTSSBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updateTSS();
      }
    });


    JLabel lblTSSNote1 = new JLabel("Note: Selection restricted to state realisation nodes");
    lblTSSNote1.setBounds(194, 258, 247, 14);
    panelTSS.add(lblTSSNote1);

    JLabel lblSelectedTSS = new JLabel("Selected target system states:");
    lblSelectedTSS.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectedTSS.setBounds(743, 14, 236, 22);
    panelTSS.add(lblSelectedTSS);
    taTSS.setEditable(false);

    taTSS.setBounds(743, 42, 345, 580);
    panelTSS.add(taTSS);
    cmbInitialState.setBounds(256, 333, 376, 32);
    panelTSS.add(cmbInitialState);

    JLabel lblTSSNote2 =
        new JLabel("Note: Selection restricted to Target System States which you have chosen.");
    lblTSSNote2.setBounds(162, 395, 361, 14);
    panelTSS.add(lblTSSNote2);

    JLabel lblInitialState = new JLabel("Select Initial State of the System:");
    lblInitialState.setBounds(85, 342, 161, 14);
    panelTSS.add(lblInitialState);
    cmbInitialState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        initialNode = cmbInitialState.getSelectedItem().toString();
      }
    });

    JPanel panelOA = new JPanel();
    tabbedPane.addTab("Observable Responses", null, panelOA, null);
    panelOA.setLayout(null);

    JLabel lblORComponent = new JLabel("Select Component Name:");
    lblORComponent.setBounds(331, 41, 121, 14);
    panelOA.add(lblORComponent);

    cmbORComponent.setBounds(462, 30, 313, 32);
    panelOA.add(cmbORComponent);

    cmbORBehaviour.setBounds(462, 73, 313, 32);
    panelOA.add(cmbORBehaviour);

    JLabel lblORBehaviour = new JLabel("Select Behaviour:");
    lblORBehaviour.setBounds(368, 88, 84, 14);
    panelOA.add(lblORBehaviour);
    taORInput.setLineWrap(true);

    taORInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent arg0) {
        if (cmbORComponent.getSelectedItem() != null && cmbORBehaviour.getSelectedItem() != null) {
          observableResponses.put(cmbORComponent.getSelectedItem().toString() + ";"
              + cmbORBehaviour.getSelectedItem().toString(), taORInput.getText());
        }
        updateOTA();
      }
    });
    taORInput.setBounds(10, 238, 637, 383);
    panelOA.add(taORInput);


    cmbORComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updateObsBehaviours();
        populateOR();
      }
    });


    cmbORBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        populateOR();
      }
    });


    JLabel lblORInput = new JLabel("Observable Response:");
    lblORInput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORInput.setBounds(10, 205, 174, 22);
    panelOA.add(lblORInput);

    JLabel lblNoteSelectionRestricted_1 =
        new JLabel(
            "Note: Selection restricted to all components except events, external inputs, selections & guards.");
    lblNoteSelectionRestricted_1.setBounds(331, 127, 464, 14);
    panelOA.add(lblNoteSelectionRestricted_1);

    JLabel lblORConfigured = new JLabel("Observable Responses configured for:");
    lblORConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORConfigured.setBounds(676, 205, 299, 22);
    panelOA.add(lblORConfigured);


    taORConfigured.setEditable(false);
    taORConfigured.setBounds(676, 238, 440, 383);

    panelOA.add(taORConfigured);

    JPanel panelUA = new JPanel();
    tabbedPane.addTab("User Actions/External Inputs", null, panelUA, null);
    panelUA.setLayout(null);

    JLabel label_5 = new JLabel("Select Component Name:");
    label_5.setBounds(303, 31, 121, 14);
    panelUA.add(label_5);

    JLabel label_6 = new JLabel("Select Behaviour:");
    label_6.setBounds(340, 75, 84, 14);
    panelUA.add(label_6);

    cmbUAComp.setBounds(434, 22, 313, 32);
    panelUA.add(cmbUABehaviour);

    cmbUABehaviour.setBounds(434, 66, 313, 32);
    panelUA.add(cmbUAComp);


    JLabel lblUserActioninput = new JLabel("Action to be taken:");
    lblUserActioninput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUserActioninput.setBounds(10, 185, 151, 22);
    panelUA.add(lblUserActioninput);
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
    txtUA.setBounds(10, 218, 637, 404);
    panelUA.add(txtUA);


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
    lblNoteSelectionRestricted_2.setBounds(340, 135, 260, 14);
    panelUA.add(lblNoteSelectionRestricted_2);


    taUserActions.setEditable(false);
    taUserActions.setBounds(676, 218, 440, 404);
    panelUA.add(taUserActions);

    JLabel lblUserActionsConfigured = new JLabel("Actions configured for:");
    lblUserActionsConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUserActionsConfigured.setBounds(676, 185, 180, 22);
    panelUA.add(lblUserActionsConfigured);
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

    chckbxAppearPreAmble.setBounds(434, 105, 481, 23);
    panelUA.add(chckbxAppearPreAmble);


    JPanel panelTP = new JPanel();

    List<TestPath> feeds = new ArrayList<TestPath>();
    feeds.add(new TestPath("ATM [READY]", "something", 5));
    feeds.add(new TestPath("Display [Transaction]", "something", 15));
    panelTP.setLayout(null);

    JTable table = new JTable(new TestPathModel(feeds));
    table.setDefaultRenderer(TestPath.class, new TestPathCell());
    table.setDefaultEditor(TestPath.class, new TestPathCell());
    table.setRowHeight(60);
    table.setBounds(731, 11, 500, 610);
    panelTP.add(table);

    tabbedPane.addTab("Join Test Paths", null, panelTP, null);
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.setBounds(0, 0, 185, 40);

    JMenu mnFile = new JMenu("File");
    menuBar.add(mnFile);
    mnFile.setMnemonic(KeyEvent.VK_F);

    /**
     * Load BT File.
     */
    JMenuItem mntmLoadBtFile = new JMenuItem("Load BT File");
    mntmLoadBtFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + System.getProperty("file.separator")
            + "models");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("BT Model", "bt", "btc");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        int retVal = chooser.showOpenDialog(frame);

        if (retVal == JFileChooser.APPROVE_OPTION) {
          File f = chooser.getSelectedFile();
          System.out.println("You chose " + f.getPath());
          filenameStr = (f.getPath()).replace("\\", "/");

          connectToServer();

          openBTFile(f.getName());
        } else if (retVal == JFileChooser.CANCEL_OPTION) {
          System.out.println("You cancelled the choice");
        } else if (retVal == JFileChooser.ERROR_OPTION) {
          printErrorMessage("error|6|");
        }

      }

      private void openBTFile(String filename) {
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
          clearEverything();
          BTModelReader modelReader = new BTModelReader(result);
          indexToNodeMap = modelReader.getIndexToNodeMap();
          tagToIndexMap = modelReader.getTagToIndexMap();
        } else {
          printErrorMessage("error|6|");
          return;
        }

        populateData();
        isLoaded = true;
        frame.setTitle("TP-Optimizer - " + filename);
      }
    });
    mnFile.add(mntmLoadBtFile);


    /**
     * Load configuration file for TCGen-UI
     */
    JMenuItem mntmSave = new JMenuItem("Load Test Case Config");
    mntmSave.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO Save Data here
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
    return menuBar;
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
      case 6:
        JOptionPane.showMessageDialog(null, "Unable to read Behaviour Tree Model");
        break;
      case 7:
        JOptionPane.showMessageDialog(
            null,
            "Unable to create folder. Please ensure write access is available in "
                + System.getProperty("user.dir"));
        break;
      case 8:
        JOptionPane.showMessageDialog(
            null,
            "An unknown error blocked this application from opening "
                + System.getProperty("user.dir") + System.getProperty("file.separator")
                + "test-cases");
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
        ((DefaultTableModel) tblNOI.getModel()).addRow(rowData);
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
    cmbTSSComponent.setModel(new DefaultComboBoxModel<String>(clean(cmbData)));
    updateTSSDisplay();
  }

  private void updateTSSDisplay() {
    Set<String> cmbBehaviourData = new HashSet<String>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getComponent().equals(
            cmbTSSComponent.getItemAt(cmbTSSComponent.getSelectedIndex()))
            && node.getBehaviourType().equals("STATE-REALISATION")) {
          cmbBehaviourData.add(node.getBehaviour());
        }
      }
    }
    cmbTSSBehaviour.setModel(new DefaultComboBoxModel<String>(clean(cmbBehaviourData
        .toArray(new String[0]))));
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

    cmbORComponent.setModel(new DefaultComboBoxModel<String>(clean(obsComps)));
    updateObsBehaviours();
  }

  private void updateObsBehaviours() {
    String[] obsBehaviours =
        new String[compToBehaviourMap.get(cmbORComponent.getSelectedItem()).size()];
    int i = 0;
    if (cmbORComponent.getModel().getSize() > 0) {
      System.out.println(cmbORComponent.getSelectedItem());
      for (NodeData node : compToBehaviourMap.get(cmbORComponent.getSelectedItem())) {
        if (!Constants.unacceptedObsBehaviourTypes.contains(node.getBehaviourType())) {
          obsBehaviours[i++] = node.getBehaviour() + " [" + node.getBehaviourType() + "]";
        }
      }
    }
    cmbORBehaviour.setModel(new DefaultComboBoxModel<String>(clean(obsBehaviours)));
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
