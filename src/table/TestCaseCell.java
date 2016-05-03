package table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import other.TestCase;
import tree.Node;

public class TestCaseCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel colourIndicator;
  JLabel id;
  JLabel text;

  TestCase testCase;

  public TestCaseCell() {
    panel = new JPanel(new BorderLayout());
    id = new JLabel();
    panel.add(id, BorderLayout.LINE_START);
    text = new JLabel();
    panel.add(text, BorderLayout.CENTER);
    colourIndicator = new JLabel("    ");
    colourIndicator.setOpaque(true);
    panel.add(colourIndicator, BorderLayout.LINE_END);
    panel.setBackground(new Color(255, 77, 77));
  }

  protected void updateData(TestCase testCase, boolean isSelected, JTable table) {
    this.testCase = testCase;
    text.setText(getCellText());
    id.setText("<html>&nbsp;&nbsp;<b>ID:</b><br>&nbsp;&nbsp;&nbsp;" + testCase.getID()
        + Constants.htmlTabSpacing);
    // If cell is selected give it a border
    if (isSelected) {
      panel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
    } else {
      panel.setBorder(table.getBorder());
    }
    if (testCase.isSelected()) {
      // If test case has been added to test path colour it blue
      panel.setBackground(Constants.testCaseSelectedColour);
    } else {
      panel.setBackground(Constants.notSelectedColour);
    }
    if (!testCase.isReachable()) {
      // If test case is unreachable colour it red
      colourIndicator.setBackground(Constants.unavailableColour);
    } else if (testCase.getUserActionsPreamble().size() > 0) {
      colourIndicator.setBackground(Constants.preambleColour);
    } else {
      colourIndicator.setBackground(Constants.immediatelyAvailableColour);
    }
    panel.setToolTipText(getToolTipText());
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    TestCase feed = (TestCase) value;
    updateData(feed, true, table);
    return panel;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    TestCase feed = (TestCase) value;
    updateData(feed, isSelected, table);
    return panel;
  }

  public Object getCellEditorValue() {
    return null;
  }

  public String getToolTipText() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>");
    sb.append("<b>Nodes Involved:</b> " + testCase.getNodeLength());
    for (Node n : testCase.getNodeSteps()) {
      sb.append("<br>" + Constants.htmlTabSpacing + formatNode(n));
    }
    sb.append("</html>");
    return sb.toString();
  }

  private String formatNode(Node n) {
    return n.toString();
  }

  private String getCellText() {
    return "<html><b>Start Node:</b> " + testCase.getStartNode() + "<br><b>End Node:</b> "
        + testCase.getFirstNodeOfEndingBlock() + "<br><b>User Actions Involved:</b> "
        + testCase.getUserActionsAmount() + "</html>";
  }
}
