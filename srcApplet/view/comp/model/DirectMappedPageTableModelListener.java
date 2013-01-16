package view.comp.model;

import view.model.DirectPageDirectoryModel;

public interface DirectMappedPageTableModelListener {

	public void tableAdded(DirectPageDirectoryModel model , int level);
	public void tablesCleared();
	public void tablesCleared(int level, int index);
	
	
}
