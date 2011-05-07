package pharoslabut.demo.simonsays;

import java.util.*;
import javax.swing.table.*;

/**
 * A table for cyber-physical attributes.  It has three columns: the instruction,
 * the logical distance, and the physical distance.
 * 
 * @author Chien-Liang Fok
 */
public class CPPData extends AbstractTableModel {
	
	private final String[] columnNames = {"Instruction", "Logical Parameter", "Physical Parameter"};
	
	private Vector<CPP> cppValues = new Vector<CPP>();
	
	/**
	 * The constructor.
	 */
	public CPPData() {
//		cppValues.add(new CPP("MOVE", 1.0, 0.8));
//		cppValues.add(new CPP("MOVE", 1.0, 0.95));
//		cppValues.add(new CPP("MOVE", 1.0, 1.05));
//		
//		new Thread(new Runnable() {
//			public void run() {
//				
//				while (true) {
//					add(new CPP("MOVE", Math.random(), Math.random()));
//					
//					synchronized(this) {
//						try {
//							wait(2000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}).start();
	}
	
	/**
	 * Add a cyber-physical property.
	 * 
	 * @param cpp the cyber-physical property to add.
	 */
	public void add(CPP cpp) {
		cppValues.add(cpp);
		fireTableRowsInserted(cppValues.size()-1, cppValues.size()-1);
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return cppValues.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		CPP cpp = cppValues.get(row);
		switch(column) {
		case 0:
			return cpp.getInstruction();
		case 1:
			return cpp.getLValue();
		case 2:
			return cpp.getPValue();
		default:
			return null;
		}
	}
	
	
}
