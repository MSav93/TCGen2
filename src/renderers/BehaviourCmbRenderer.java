package renderers;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import tree.Node;

public class BehaviourCmbRenderer extends DefaultListCellRenderer {

  private static final long serialVersionUID = -5360004325562035402L;

  public Component getListCellRendererComponent(JList<?> list, Object value, int index,
      boolean isSelected, boolean cellHasFocus) {
    if (value instanceof Node) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setText("[" + ((Node) value).getTag() + "] " + ((Node) value).getBehaviour());
      setFont(list.getFont());
    } else if (value != null) {
      setText(value.toString());
    }
    return this;
  }
}
