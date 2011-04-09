package pharoslabut;
public class LinkedList {
	private ListNode header;
	
	public LinkedList(){
		header = new ListNode(null);
	}
	
	public boolean contains(Node x){
		boolean contains = false;
		LinkedListIterator itr;
		itr = this.zeroth();
		while(itr.isValid()){
			if(itr.retrieve()==x)
				contains = true;
			itr.advance();
		}
		return contains;
	}
	
	public boolean isEmpty(){
		return header.next == null;
	}
	
    public LinkedListIterator zeroth(){
        return new LinkedListIterator(header);
    }
	
	public LinkedListIterator first(){
		return new LinkedListIterator(header.next);
	}
	
	public void insert(Node x, LinkedListIterator p){
		if(p!=null&&p.current!=null)
			p.current.next = new ListNode(x,p.current.next);
	}
	
    public LinkedListIterator find(Node x){
        ListNode itr = header.next;
        
        while(itr!= null&&!itr.element.equals(x))
            itr = itr.next;
        
        return new LinkedListIterator(itr);
    }
	
	public LinkedListIterator findPrevious(Node x){
		ListNode itr = header;
		
		while(itr.next!=null&&!itr.next.element.equals(x))
			itr = itr.next;
		
		return new LinkedListIterator(itr);
	}
	
	public void remove(Node x){
		LinkedListIterator p = findPrevious(x);
		if(p.current.next!=null)
			p.current.next = p.current.next.next;
	}
	
    public static void printList(LinkedList theList) {
        if(theList.isEmpty())
            System.out.print( "Empty list" );
        else{
            LinkedListIterator itr = theList.first();
            for(;itr.isValid();itr.advance())
            	itr.retrieve().getInfo();
        }
        
        System.out.println();
    }
    
    
    public static int listSize(LinkedList theList){
        LinkedListIterator itr;
        int size = 0;
        for(itr = theList.first();itr.isValid();itr.advance())
            size++;
        
        return size;
    }
    
}
