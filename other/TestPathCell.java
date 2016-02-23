package other;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class TestPathCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;
//  JButton showButton;

  TestPath path;

  public TestPathCell() {
    text = new JLabel();
//    showButton = new JButton("View Articles");
//    showButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent arg0) {
//        JOptionPane.showMessageDialog(null, "Reading " + path.name);
//      }
//    });

    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
//    panel.add(showButton);
    panel.setBackground(new Color(255, 77, 77));
  }

  private void updateData(TestPath path, boolean isSelected, JTable table) {
    this.path = path;

    text.setText("<html><b>TargetNode: " + path.name + "</b><br>" + path.something
        + "<br>Steps Away: " + path.steps + "</html>");
    Color bg = table.getSelectionBackground();

    if (isSelected) {
      // if(path.completed) {
      panel.setBackground(new Color(Math.min(255, bg.getRed() + 100), bg.getGreen(), bg.getBlue(),
          bg.getAlpha()));
      // } else {
      // panel.setBackground(bg);
    } else {
      // if(path.completed) {
      // panel.setBackground(new Color(255, 77, 77);
      // } else {
      panel.setBackground(table.getBackground());
      // }
    }
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    TestPath feed = (TestPath) value;
    updateData(feed, true, table);
    return panel;
  }

  public Object getCellEditorValue() {
    return null;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    TestPath feed = (TestPath) value;
    updateData(feed, isSelected, table);
    return panel;
  }
}
