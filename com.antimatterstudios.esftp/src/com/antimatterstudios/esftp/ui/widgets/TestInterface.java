package com.antimatterstudios.esftp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.antimatterstudios.esftp.Activator;
import com.antimatterstudios.esftp.Transfer;
import com.antimatterstudios.esftp.TransferDetails;
import com.antimatterstudios.esftp.ui.UserInterface;

public class TestInterface extends Composite {
	protected Button m_test;
	protected Text m_output;
	protected UserInterface m_userInterface;
	
	protected Listener m_testListener;
	
	public TestInterface(Composite parent, int style, UserInterface userInterface)
	{
		super(parent,style);
		
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		m_userInterface = userInterface;
		
		m_testListener = new Listener(){
			public void handleEvent(Event e){
				if(e.widget == m_test) test();
			}
		};
			
		m_output = new Text(this, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		m_output.setText("<ESFTP Plugin (version: "+Activator.getDefault().getVersion()+") > Ready to test");
		
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		m_output.setLayoutData(data);
		
		m_test = new Button(this, SWT.NONE);
		m_test.setText("Test settings");
		m_test.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		m_test.addListener(SWT.Selection, m_testListener);
	}
	
	public void test(){
		m_output.setText("<Testing SFTP Site>\nPlease Wait....\n");
		m_output.update();
		
		m_userInterface.updateStore();
		TransferDetails details = new TransferDetails(m_userInterface.getPreferences());
		Transfer transfer = Activator.getDefault().getTransfer(details.getProtocol());
		if(transfer != null){
			System.out.println("TI::test(), Transfer class = "+transfer.getClass().getName());
			transfer.init(details);
			
			try{
				System.out.println("TI::test(), calling test");
				m_userInterface.setVerified(transfer.test());
			}catch(NullPointerException e){
				System.out.println("NPE detected whilst testing and verifying the esftp details");
				System.out.println("TI::test(), NPE caught");
			}
			System.out.println("TI::test(), outputting everything");
			m_output.append( transfer.getTransferOuput() );
			System.out.println("TI::test(), test completed");
		}else System.out.println("transfer object was null");
	}
}
