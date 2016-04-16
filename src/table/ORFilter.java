package table;

import javax.swing.RowFilter;

import other.Constants;
import tree.Node;

public class ORFilter extends RowFilter<NodeTableModel, Integer> {

  public boolean include(RowFilter.Entry<? extends NodeTableModel, ? extends Integer> entry) {
    NodeTableModel personModel = entry.getModel();
    Node node = personModel.getValueAt(entry.getIdentifier(), 0);
    if (Constants.acceptedORBehaviourTypes.contains(node.getBehaviourType())
        && Constants.acceptedORFlags.contains(node.getFlag())) {
      return true;
    }
    return false;
  }

}
