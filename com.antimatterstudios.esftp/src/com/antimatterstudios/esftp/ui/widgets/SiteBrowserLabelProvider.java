package com.antimatterstudios.esftp.ui.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.antimatterstudios.esftp.Activator;

public class SiteBrowserLabelProvider extends LabelProvider
{		
	private Map imageCache = new HashMap(11);
	
	/*
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		ImageDescriptor descriptor = null;
		if (element instanceof Directory) {
			descriptor = Activator.getImageDescriptor("icons/directory.png");
		}else if(element instanceof File){
			descriptor = Activator.getImageDescriptor("icons/file.png");
		}else{
			throw unknownElement(element);
		}

		//obtain the cached image corresponding to the descriptor
		Image image = null;
		if(descriptor != null){
			image = (Image)imageCache.get(descriptor);
			if (image == null) {
				image = descriptor.createImage();
				imageCache.put(descriptor, image);
			}
		}
		return image;
	}

	/*
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		Model m = (Model)element;
		if (m instanceof Model) {
			if(m.getName() == null) {
				return "";
			} else {
				return m.getName();
			}
		} else {
			throw unknownElement(element);
		}
	}
	
	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}