package org.impetuouslab.eclipse.filecompletion;

import java.io.File;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class FileCompletionPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final Logger LOG = Logger
			.getLogger(FileCompletionPreferencePage.class.getName());

	static {
		LOG.info("filecompletion : static init done");
	}
	
	private volatile String currentValue;
	
	
	public static volatile boolean checkDuringTyping = false;

	private static volatile boolean checkDuringTypingProposed = false;
	
	public FileCompletionPreferencePage() {
	}

	public FileCompletionPreferencePage(String title) {
		super(title);
	}

	public FileCompletionPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void init(IWorkbench workbench) {
		LOG.info("filecompletion : about to init ..");
		currentValue = FileCompletionActivator.getDefault()
				.getPreferenceStore()
				.getString(FileCompletionActivator.openFileWithExternalProgramPerfId);
		
		checkDuringTyping=FileCompletionActivator.getDefault()
				.getPreferenceStore()
				.getBoolean(FileCompletionActivator.checkDuringTypingId);
		
		checkDuringTypingProposed=checkDuringTyping;
		
		setPreferenceStore(FileCompletionActivator.getDefault()
				.getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent2) {
		currentValue = FileCompletionActivator.getDefault()
				.getPreferenceStore()
				.getString(FileCompletionActivator.openFileWithExternalProgramPerfId);
		final Composite parent = new Composite(parent2, SWT.NULL);
		LOG.info("current value = " + currentValue);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);
		Label label = new Label(parent, SWT.NONE);
		label.setText("Open file with external program");
		final Text text = new Text(parent, SWT.BORDER);
		text.addVerifyListener(new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				Display.getCurrent().asyncExec(new Runnable() {

					public void run() {
						// used async to get final text
						LOG.info(text.getText());
						currentValue = text.getText();
						setValid(isValid());
					}
				});
			}
		});
		Button button = new Button(parent, SWT.NONE);
		button.setText("Select file");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell(), SWT.OPEN);
				fd.setFileName(text.getText());
				fd.setText("Select");
				currentValue = fd.open();
				currentValue = currentValue.replace('\\', '/');
				LOG.info("selected file " + currentValue);
				text.setText(currentValue);
				setValid(isValid());

			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		final Button doVerifictionOnline = new Button(parent, SWT.CHECK);		
		doVerifictionOnline.setText("Check file name during typing");
		doVerifictionOnline.setSelection(checkDuringTyping);
		checkDuringTypingProposed=checkDuringTyping;
		doVerifictionOnline.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				checkDuringTypingProposed = doVerifictionOnline.getSelection();				
			}
			
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
				
			}
		});
		if (currentValue != null) {
			text.setText(currentValue);
		}
		isValid();
		return parent;
	}

	@Override
	public boolean isValid() {
		if (currentValue == null) {
			setMessage("Specify file",
					org.eclipse.jface.dialogs.IMessageProvider.ERROR);
		}
		boolean fileExists = new File(currentValue).isFile();
		if (fileExists) {
			setMessage(null);
		} else {
			setMessage("File not found",
					org.eclipse.jface.dialogs.IMessageProvider.ERROR);
		}
		return fileExists;
	}

	@Override
	public boolean performOk() {
		if (isValid()) {
			// storing preference
			FileCompletionActivator
					.getDefault()
					.getPreferenceStore()
					.setValue(
							FileCompletionActivator.openFileWithExternalProgramPerfId,
							currentValue);
			
			checkDuringTyping=checkDuringTypingProposed;
			
			FileCompletionActivator
				.getDefault()
				.getPreferenceStore()
				.setValue(
						FileCompletionActivator.checkDuringTypingId,
						checkDuringTyping);
		} else {
			return false;
		}

		return super.performOk();
	}

}
