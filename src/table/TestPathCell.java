package table;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

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

public class TestPathCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;
  TestCase testCase;

  public TestPathCell() {
    text = new JLabel();
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
    panel.setBackground(new Color(255, 77, 77));
  }

  protected void updateData(TestCase testCase, boolean isSelected, JTable table) {
    this.testCase = testCase;
    text.setText(getCellText());

    // If cell is selected give it a border
    if (isSelected) {
      panel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
    } else {
      panel.setBorder(table.getBorder());
    }
    if (testCase.isPreAmble()) {
      panel.setBackground(Constants.warningColour);
    } else {
      panel.setBackground(Constants.notSelectedColour);
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

  protected String getCellText() {
    return "<html><b>Start Node:</b> " + testCase.getLastNodeOfStartingBlock() + "<br><b>End Node:</b> "
        + testCase.getEndNode() + "<br><b>Steps Involved:</b> " + (testCase.getNodeLength() - 1) + "</html>";
  }

  public String getToolTipText() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><b>Steps Involved:</b> " + testCase.getNodeLength());
    for (Node n : testCase.getNodeSteps()) {
      sb.append("<br>" + Constants.htmlTabSpacing + formatNode(n));
    }
    sb.append("</html>");
    return sb.toString();
  }

  private String formatNode(Node n) {
    return "[" + n.getTag() + "] " + n.getComponent() + ": " + n.getBehaviour() + "["
        + n.getBehaviourType() + "]" + getFlagSymbol(n.getFlag());
  }

  private String getFlagSymbol(String flag) {
    if (!flag.equals("")) {
      if (Constants.nodeFlags.contains(flag)) {
        return " " + Constants.nodeFlagSymbols.get(Constants.nodeFlags.indexOf(flag));
      }
      return " " + flag;
    }
    return "";
  }
}
