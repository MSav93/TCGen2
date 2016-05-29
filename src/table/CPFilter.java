package table;

import javax.swing.RowFilter;

import other.Constants;
import tree.Node;

public class CPFilter extends RowFilter<NodeTableModel, Integer> {

  public boolean include(RowFilter.Entry<? extends NodeTableModel, ? extends Integer> entry) {
    NodeTableModel personModel = entry.getModel();
    Node node = personModel.getValueAt(entry.getIdentifier(), 0);
    if (Constants.acceptedCPBehaviourTypes.contains(node.getBehaviourType())
        && Constants.acceptedCPFlags.contains(node.getFlag())) {
      for (int i = 0; i < entry.getIdentifier(); i++) {
        Node n = personModel.getValueAt(i);
        if (i != entry.getIdentifier()) {
          if (n.getComponent().equals(node.getComponent())
              && n.getBehaviour().equals(node.getBehaviour())) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }
}
