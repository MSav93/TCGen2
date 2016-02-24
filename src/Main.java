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
import java.io.IOException;
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
   * Application
   */
  private static Main frame = null;

  /**
   * Main Panel
   */
  private JPanel contentPane;

  /**
   * Components on Main Panel
   */
  private JTabbedPane tabbedPane;
  private JButton btnGenerateTestCases;
  private JButton btnGenerateExcel;

  /**
   * Components on first tab
   */
  private final JLabel lblSelectNodes = new JLabel("Select nodes of interest:");
  private JTable tblNOI;

  /**
   * Components on second tab
   */
  private JComboBox<String> cmbCPComponent = new JComboBox<String>();
  private JComboBox<String> cmbCPBehaviour = new JComboBox<String>();
  private final JCheckBox chkCP = new JCheckBox("This is a Target System State");
  private JComboBox<String> cmbInitialState = new JComboBox<String>();
  private final TextArea taCP = new TextArea();

  /**
   * Components on third tab
   */
  private JComboBox<String> cmbORComponent = new JComboBox<String>();
  private JComboBox<String> cmbORBehaviour = new JComboBox<String>();
  private final JTextArea taORInput = new JTextArea();
  private final TextArea taORConfigured = new TextArea();

  /**
   * Components on fourth tab
   */
  private JComboBox<String> cmbUAComp = new JComboBox<String>();
  private JComboBox<String> cmbUABehaviour = new JComboBox<String>();
  private final TextArea taUAConfigured = new TextArea();
  private final JTextArea taUAInput = new JTextArea();
  private final JCheckBox chckbxAppearPreAmble =
      new JCheckBox(
          "This condition should appear in the Pre-Amble/This is an External Input not under user's control");

  /**
   * Maps to store info about BT Model
   */
  private TreeMap<Integer, BTNode> indexToNodeMap = new TreeMap<Integer, BTNode>();
  HashMap<String, Integer> tagToIndexMap = new HashMap<>(); // map tag to block index
  private TreeMap<String, NodeData> tagToNodeDataMap = new TreeMap<String, NodeData>();
  private HashMap<String, ArrayList<NodeData>> compToBehaviourMap =
      new HashMap<String, ArrayList<NodeData>>();

  /**
   * Variables that must be saved
   */
  private String btXML;
  private String filenameStr;
  private String initialNode;

  /**
   * Other
   */
  private SBCLPipe sbcl = new SBCLPipe();



  private static final long serialVersionUID = 1L;
  LinkedHashMap<String, ArrayList<ArrayList<Element>>> testPaths =
      new LinkedHashMap<String, ArrayList<ArrayList<Element>>>();
  HashMap<String, ArrayList<NodeData>> mapToTag2 = new HashMap<>(); // map block index to tag
  public LinkedHashMap<String, String[]> listOfTestPaths = new LinkedHashMap<>();


  private Boolean isLoaded = false;

  private String[] potentialCP = new String[0];

  HashMap<String, Boolean> chosenCPs = new HashMap<>();
  HashMap<String, String> observableResponses = new HashMap<>();
  HashMap<String, String[]> userActions = new HashMap<>();

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e1) {
      System.err
          .println("System 'Look and Feel' could not be found. Using Windows 'Look and Feel'");
    }
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e1) {
      System.err
          .println("Windows 'Look and Feel' could not be found. Using default 'Look and Feel'");
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
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
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
    cmbCPComponent.removeAllItems();
    cmbCPBehaviour.removeAllItems();
    cmbORComponent.removeAllItems();
    cmbORBehaviour.removeAllItems();
    cmbUAComp.removeAllItems();
    cmbUABehaviour.removeAllItems();

    testPaths = new LinkedHashMap<String, ArrayList<ArrayList<Element>>>();

    listOfTestPaths = new LinkedHashMap<>();

    chosenCPs = new HashMap<>();
    observableResponses = new HashMap<>();
    userActions = new HashMap<>();
    mapToTag2 = new HashMap<>();
    tagToIndexMap = new HashMap<>();
    indexToNodeMap = new TreeMap<>();

    potentialCP = new String[0];

    chkCP.setSelected(false);

    chckbxAppearPreAmble.setSelected(false);
    taORInput.setText("");
    taUAInput.setText("");

    taCP.setText("");

    taORConfigured.setText("");
    taUAConfigured.setText("");

    filenameStr = "";

    updateCPTA();
    updateOTA();
    updateUATA();
  }

  /**
   * Write label that shows configured CP nodes.
   */
  public void updateCPTA() {
    taCP.setText("");
    for (Entry<String, Boolean> node : chosenCPs.entrySet()) {
      String key = node.getKey();
      Boolean value = node.getValue();

      if (value == true) {
        if (taCP.getText().equals("")) {
          taCP.setText(taCP.getText() + key);
        } else {
          taCP.setText(taCP.getText() + ", \n" + key);
        }
      }
    }
    taCP.setText(taCP.getText().replaceAll(";", ": "));
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
    taUAConfigured.setText("");
    for (Entry<String, String[]> component : userActions.entrySet()) {
      String key = component.getKey();
      String[] value = component.getValue();

      if (!value[0].equals("")) {
        if (taUAConfigured.getText().equals("")) {
          taUAConfigured.setText(taUAConfigured.getText() + key);
        } else {
          taUAConfigured.setText(taUAConfigured.getText() + ", \n" + key);
        }
      }
    }
    taUAConfigured.setText(taUAConfigured.getText().replaceAll(";", ": "));
  }


  /**
   * Populate configuration in Range.
   */
  public void updateRangeTab() {
    potentialCP = new String[indexToNodeMap.size()];
    int i = 0;
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        for (Entry<String, Boolean> selectedCP : chosenCPs.entrySet()) {
          String cp = selectedCP.getKey();
          Boolean selected = selectedCP.getValue();
          if (selected) {
            String[] cpParts = cp.split(";");
            if (node.getComponent().equals(cpParts[0]) && node.getBehaviour().equals(cpParts[1])) {

              if (node.getBehaviourType().equals("STATE-REALISATION")) {
                potentialCP[i] =
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
    potentialCP = clean(potentialCP);
    Arrays.sort(potentialCP, new AlphanumComparator());
    cmbInitialState.setModel(new DefaultComboBoxModel<String>(potentialCP));
    setRange();
  }

  /**
   * Populate configuration in Range by component types.
   */
  public void updateRangeTabComponent() {
    Set<String> set = new HashSet<String>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        for (Entry<String, Boolean> selectedCP : chosenCPs.entrySet()) {
          String cp = selectedCP.getKey();
          Boolean selected = selectedCP.getValue();
          if (selected) {
            String[] cpParts = cp.split(";");
            if (node.getComponent().equals(cpParts[0]) && node.getBehaviour().equals(cpParts[1])) {
              if (node.getBehaviourType().equals("STATE-REALISATION")) {
                set.add(node.getComponent() + " - " + node.getBehaviour());
              }
            }
          }
        }
      }
    }
    potentialCP = set.toArray(new String[0]);
    Arrays.sort(potentialCP, new AlphanumComparator());

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
   * Populate configuration for CP.
   */
  public void updateCP() {
    String selectedCP =
        cmbCPComponent.getSelectedItem().toString() + ";"
            + cmbCPBehaviour.getSelectedItem().toString();

    if (chosenCPs.get(selectedCP) != null) {
      if (chosenCPs.get(selectedCP) == true) {
        chkCP.setSelected(true);
      } else {
        chkCP.setSelected(false);
      }
    } else {
      chkCP.setSelected(false);
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
        taUAInput.setText(userAction[0]);

        if (userAction[1].equals("true")) {
          chckbxAppearPreAmble.setSelected(true);
        } else {
          chckbxAppearPreAmble.setSelected(false);
        }
      } else {
        taUAInput.setText("");
        chckbxAppearPreAmble.setSelected(false);
      }
    } catch (Exception e) {

    }
  }


  /**
   * Populate test case configuration in TCC file format.
   */

  public void SaveConfig() {
    JFrame parentFrame = new JFrame();

    JFileChooser fileChooser = new JFileChooser();

    javax.swing.filechooser.FileFilter filter =
        new FileNameExtensionFilter("Test Case Config file (.TCC)", new String[] {"TCC"});
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);

    fileChooser.setDialogTitle("Specify a file to save");

    int userSelection = fileChooser.showSaveDialog(parentFrame);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
      // TODO Save File here
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
    createMainButtons(panelMain);

    contentPane.add(createMenuBar());

    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBounds(10, 131, 1131, 660);
    contentPane.add(tabbedPane);

    tabbedPane.addTab(Constants.tab1Name, null, createNOITab(), null);
    tabbedPane.addTab(Constants.tab2Name, null, createCPTab(), null);
    tabbedPane.addTab(Constants.tab3Name, null, createOATab(), null);
    tabbedPane.addTab(Constants.tab4Name, null, createUATab(), null);
    tabbedPane.addTab(Constants.tab5Name, null, createTPTab(), null);

    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      tabbedPane.setEnabledAt(i, false);
    }
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.setBounds(0, 0, 1158, 21);

    JMenu mnFile = new JMenu("File");
    menuBar.add(mnFile);
    mnFile.setMnemonic(KeyEvent.VK_F);

    /**
     * Load BT File.
     */
    JMenuItem mntmLoadBTModel = new JMenuItem("Load BT File");
    mntmLoadBTModel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser =
            new JFileChooser(System.getProperty("user.dir") + System.getProperty("file.separator")
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

          String result = connectToServer();
          
          if(!(result.equals("") || result.contains("error"))) {
            loadBTFile(f.getName());
          }
        } else if (retVal == JFileChooser.CANCEL_OPTION) {
          System.out.println("You cancelled the choice");
        } else if (retVal == JFileChooser.ERROR_OPTION) {
          printErrorMessage("error|6|");
        }
      }

      private void loadBTFile(String filename) {
        // load bt-file into BTAnalyser
        System.out.println("Processing bt file");
        sbcl.sendCommand("(process-bt-file \"" + filenameStr + "\")");

        // important to ensure resulting TCPs are reachable/valid
        System.out.println("Ensure TCPs are reachable");
        sbcl.sendCommand("(reachable-states)");

        tagToIndexMap = new HashMap<>();

        System.out.println("Building BT");
        btXML = sbcl.sendCommand("(print-bt)");
        if (!btXML.equals("<result><error>No Behavior Tree loaded.</error></result>")) {
          clearEverything();
          BTModelReader modelReader = new BTModelReader(btXML);
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
    mnFile.add(mntmLoadBTModel);


    /**
     * Load configuration file for TCGen-UI
     */
    JMenuItem mntmLoadConfig = new JMenuItem("Load Test Case Config");
    mntmLoadConfig.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO Load Config here
      }
    });
    mnFile.add(mntmLoadConfig);

    JMenuItem mntmSaveConfig = new JMenuItem("Save Test Case Config");
    mntmSaveConfig.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SaveConfig();
      }
    });
    mnFile.add(mntmSaveConfig);

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
            SaveConfig();
            System.exit(0);
          }
          System.exit(0);
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

  private void createMainButtons(JPanel panelMain) {

    btnGenerateExcel = new JButton("Generate Excel Output");
    btnGenerateExcel.setEnabled(false);
    btnGenerateExcel.setBounds(425, 59, 281, 51);
    btnGenerateExcel.setEnabled(false);
    /**
     * Generate natural language test cases in Excel format.
     */
    btnGenerateExcel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // TODO Output to Excel
      }
    });
    contentPane.add(btnGenerateExcel);

    btnGenerateTestCases = new JButton("Generate Test Cases");
    btnGenerateTestCases.setEnabled(false);
    btnGenerateTestCases.setBounds(72, 59, 281, 51);
    /**
     * Call API and generate TCPs, pre-amble and post-amble.
     */
    btnGenerateTestCases.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab5Name), true);
      }
    });
    contentPane.setLayout(null);
    contentPane.add(panelMain);
    contentPane.add(btnGenerateTestCases);

    JButton btnOpenTestCase = new JButton("Open Test Case Folder");
    btnOpenTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openFolder();
      }
    });
    btnOpenTestCase.setBounds(778, 59, 301, 51);
    contentPane.add(btnOpenTestCase);
  }

  private JPanel createNOITab() {
    JPanel panelNOI = new JPanel();

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
    return panelNOI;
  }

  private JPanel createCPTab() {
    JPanel panelCP = new JPanel();

    panelCP.setLayout(null);

    JLabel lblCPComponent = new JLabel("Select Component Name:");
    lblCPComponent.setBounds(125, 136, 121, 14);
    panelCP.add(lblCPComponent);

    JLabel lblCPBehaviour = new JLabel("Select Behaviour:");
    lblCPBehaviour.setBounds(162, 183, 84, 14);
    panelCP.add(lblCPBehaviour);

    cmbCPComponent.setBounds(256, 127, 313, 32);
    panelCP.add(cmbCPComponent);

    cmbCPBehaviour.setBounds(256, 174, 313, 32);
    panelCP.add(cmbCPBehaviour);

    chkCP.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        if (cmbCPComponent.getSelectedItem() != null && cmbCPBehaviour.getSelectedItem() != null) {
          String selectedCP =
              cmbCPComponent.getSelectedItem().toString() + ";"
                  + cmbCPBehaviour.getSelectedItem().toString();

          if (chkCP.isSelected()) {
            chosenCPs.put(selectedCP, true);

          } else {
            chosenCPs.put(selectedCP, false);
          }

          updateRangeTab();
        }
        updateCPTA();
      }
    });
    chkCP.setBounds(256, 224, 165, 23);
    panelCP.add(chkCP);

    cmbCPComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent action) {
        System.out.println(action.getActionCommand());
        if (action.getActionCommand().equals("comboBoxChanged")) {
          updateCPDisplay();
        }
      }
    });

    cmbCPBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updateCP();
      }
    });


    JLabel lblCPNote1 = new JLabel("Note: Selection restricted to state realisation nodes");
    lblCPNote1.setBounds(194, 258, 247, 14);
    panelCP.add(lblCPNote1);

    JLabel lblSelectedCP = new JLabel("Selected checkpoints:");
    lblSelectedCP.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectedCP.setBounds(743, 14, 236, 22);
    panelCP.add(lblSelectedCP);
    taCP.setEditable(false);

    taCP.setBounds(743, 42, 345, 580);
    panelCP.add(taCP);
    cmbInitialState.setBounds(256, 333, 376, 32);
    panelCP.add(cmbInitialState);

    JLabel lblCPNote2 =
        new JLabel("Note: Selection restricted to Target System States which you have chosen.");
    lblCPNote2.setBounds(162, 395, 361, 14);
    panelCP.add(lblCPNote2);

    JLabel lblInitialState = new JLabel("Select Initial State of the System:");
    lblInitialState.setBounds(85, 342, 161, 14);
    panelCP.add(lblInitialState);
    cmbInitialState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        initialNode = cmbInitialState.getSelectedItem().toString();
      }
    });
    return panelCP;
  }

  private JPanel createOATab() {
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
    return panelOA;
  }

  private JPanel createUATab() {
    JPanel panelUA = new JPanel();
    tabbedPane.addTab("User Actions/External Inputs", null, panelUA, null);
    panelUA.setLayout(null);

    JLabel lblUAComponent = new JLabel("Select Component Name:");
    lblUAComponent.setBounds(303, 31, 121, 14);
    panelUA.add(lblUAComponent);

    JLabel lblUABehaviour = new JLabel("Select Behaviour:");
    lblUABehaviour.setBounds(340, 75, 84, 14);
    panelUA.add(lblUABehaviour);

    cmbUAComp.setBounds(434, 22, 313, 32);
    panelUA.add(cmbUABehaviour);

    cmbUABehaviour.setBounds(434, 66, 313, 32);
    panelUA.add(cmbUAComp);


    JLabel lblUAinput = new JLabel("Action to be taken:");
    lblUAinput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUAinput.setBounds(10, 185, 151, 22);
    panelUA.add(lblUAinput);
    taUAInput.setLineWrap(true);

    taUAInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (cmbUAComp.getSelectedItem() != null && cmbUABehaviour.getSelectedItem() != null
            && chckbxAppearPreAmble.isSelected() == true) {
          String[] userAction = new String[2];
          userAction[0] = taUAInput.getText();
          userAction[1] = "true";
          userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString(), userAction);
        } else if (cmbUAComp.getSelectedItem() != null && cmbUABehaviour.getSelectedItem() != null
            && chckbxAppearPreAmble.isSelected() == false) {

          String[] userAction = new String[2];
          userAction[0] = taUAInput.getText();
          userAction[1] = "false";
          userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString(), userAction);
        }
        updateUATA();
      }
    });
    taUAInput.setBounds(10, 218, 637, 404);
    panelUA.add(taUAInput);


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


    taUAConfigured.setEditable(false);
    taUAConfigured.setBounds(676, 218, 440, 404);
    panelUA.add(taUAConfigured);

    JLabel lblUserActionsConfigured = new JLabel("Actions configured for:");
    lblUserActionsConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUserActionsConfigured.setBounds(676, 185, 180, 22);
    panelUA.add(lblUserActionsConfigured);
    chckbxAppearPreAmble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!taUAInput.getText().equals("")) {
          if (chckbxAppearPreAmble.isSelected() == true) {
            String[] userAction = new String[2];
            userAction[0] = taUAInput.getText();
            userAction[1] = "true";
            userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
                + cmbUABehaviour.getSelectedItem().toString(), userAction);
          } else {
            String[] userAction = new String[2];
            userAction[0] = taUAInput.getText();
            userAction[1] = "false";
            userActions.put(cmbUAComp.getSelectedItem().toString() + ";"
                + cmbUABehaviour.getSelectedItem().toString(), userAction);
          }
        }
      }
    });

    chckbxAppearPreAmble.setBounds(434, 105, 481, 23);
    panelUA.add(chckbxAppearPreAmble);
    return panelUA;
  }

  private JPanel createTPTab() {
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
    return panelTP;
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

    populateCPTab();
    populateObsTab();
    populateUATab();

    updateCPTA();
    updateOTA();
    updateUATA();

    btnGenerateTestCases.setEnabled(true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab1Name), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab2Name), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab3Name), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab4Name), true);
  }

  private void readNodes() {
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        tagToNodeDataMap.put(node.getTag(), node);
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

  private void populateCPTab() {
    Map<String, ArrayList<String>> cpComponents = new HashMap<String, ArrayList<String>>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getBehaviourType().equals("STATE-REALISATION")) {
          if (!cpComponents.containsKey(node.getComponent())) {
            cpComponents.put(node.getComponent(), new ArrayList<String>());
          }
          cpComponents.get(node.getComponent()).add(node.getBehaviour());
          chosenCPs.put(node.getComponent() + ";" + node.getBehaviour(), false);
        }
      }
    }

    String[] cmbData = cpComponents.keySet().toArray(new String[0]);
    cmbCPComponent.setModel(new DefaultComboBoxModel<String>(clean(cmbData)));
    updateCPDisplay();
  }

  private void updateCPDisplay() {
    Set<String> cmbBehaviourData = new HashSet<String>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getComponent().equals(cmbCPComponent.getItemAt(cmbCPComponent.getSelectedIndex()))
            && node.getBehaviourType().equals("STATE-REALISATION")) {
          cmbBehaviourData.add(node.getBehaviour());
        }
      }
    }
    cmbCPBehaviour.setModel(new DefaultComboBoxModel<String>(clean(cmbBehaviourData
        .toArray(new String[0]))));
    updateCP();
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

  private String connectToServer() {
    System.out.println("Connecting to server");
    String connectionResult = "";
    try {
      connectionResult = sbcl.connect("localhost", 12);
      if (!connectionResult.equals("success")) {
        printErrorMessage(connectionResult);
        return connectionResult;
      }
    } catch (InterruptedException | IOException e) {
      // nop
    }
    System.out.println("Successfully connected");
    return connectionResult;
  }
}
