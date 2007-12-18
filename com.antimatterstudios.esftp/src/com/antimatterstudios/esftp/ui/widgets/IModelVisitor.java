package com.antimatterstudios.esftp.ui.widgets;

public interface IModelVisitor {
	public void visitFolder(Model folder, Object passAlongArgument);
	public void visitFile(Model file, Object passAlongArgument);
}
