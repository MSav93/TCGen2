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

public class TestPathCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;
  JLabel id;
  TestCase testCase;

  public TestPathCell() {
    panel = new JPanel(new BorderLayout());
    id = new JLabel();
    panel.add(id, BorderLayout.LINE_START);
    text = new JLabel();
    panel.add(text, BorderLayout.CENTER);
    panel.setBackground(Constants.notSelectedColour);
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
    return "<html><b>Start Node:</b> " + testCase.getStartNode() + "<br><b>End Node:</b> "
        + testCase.getFirstNodeOfEndingBlock() + "<br><b>User Actions:</b> "
        + testCase.getUserActionsAmount() + "</html>";
  }

  public String getToolTipText() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><b>Nodes Involved:</b> " + testCase.getNodeLength());
    for (Node n : testCase.getNodeSteps()) {
      sb.append("<br>" + Constants.htmlTabSpacing + n.toString());
    }
    sb.append("</html>");
    return sb.toString();
  }
}
