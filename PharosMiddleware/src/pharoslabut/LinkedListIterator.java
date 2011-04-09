package pharoslabut;
public class LinkedListIterator {
    /**
     * Construct the list iterator
     * @param theNode any node in the linked list.
     */
    LinkedListIterator(ListNode theNode) {
        current = theNode;
    }
    
    /**
     * Test if the current position is a valid position in the list.
     * @return true if the current position is valid.
     */
    public boolean isValid(){
        return current != null;
    }
    
    /**
     * Return the item stored in the current position.
     * @return the stored item or null if the current position
     * is not in the list.
     */
    public Node retrieve(){
        return isValid() ? current.element : null;
    }
    
    /**
     * Advance the current position to the next node in the list.
     * If the current position is null, then do nothing.
     */
    public void advance() {
        if(isValid())
            current = current.next;
    }
    
    ListNode current;    // Current position
}

class ListNode{
	public Node element;
	public ListNode next;
	
    public ListNode(Node theElement) {
        this(theElement, null);
    }
    
    public ListNode(Node theElement, ListNode n) {
        element = theElement;
        next    = n;
    }
    
    public Node getElement(){
    	return element;
    }
}