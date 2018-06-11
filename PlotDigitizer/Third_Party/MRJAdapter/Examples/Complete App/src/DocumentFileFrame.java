import net.roydesign.io.ApplicationFile;
import net.roydesign.io.DocumentFile;
import net.roydesign.ui.ApplicationDialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DocumentFileFrame extends Frame
{
	private DocumentFile documentFile;
	private Button chooseFile;
	private TextField path;
	private TextField title;
	private TextField extension;
	private TextField macCreator;
	private TextField macType;
	private Button save;
	private Button open;
	
	public DocumentFileFrame()
	{
		super("Document File Test");
		setMenuBar(new MainMenuBar((Test)Test.getInstance()));
		
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dispose();
				}
			});
		
		Panel p = new Panel(new GridBagLayout());
		add(p, BorderLayout.NORTH);
		
		TextListener tl = new TextListener()
			{
				public void textValueChanged(TextEvent e)
				{
					save.setEnabled(true);
				}
			};
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		chooseFile = new Button("Choose Document...");
		chooseFile.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doChooseFile();
				}
			});
		p.add(chooseFile, gbc);
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Path:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 100;
		path = new TextField(40);
		path.setEditable(false);
		p.add(path, gbc);
		
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		p.add(new Label("Title:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		title = new TextField(20);
		title.addTextListener(tl);
		title.setEnabled(false);
		p.add(title, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Mac Creator:"), gbc);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.WEST;
		macCreator = new TextField(5);
		macCreator.addTextListener(tl);
		macCreator.setEnabled(false);
		p.add(macCreator, gbc);
		
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Extension:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		extension = new TextField(5);
		extension.addTextListener(tl);
		extension.setEnabled(false);
		p.add(extension, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Mac Type:"), gbc);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.WEST;
		macType = new TextField(5);
		macType.addTextListener(tl);
		macType.setEnabled(false);
		p.add(macType, gbc);
		
		gbc.gridy = 4;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.EAST;
		Panel bottom = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		p.add(bottom, gbc);
		
		save = new Button("Save");
		save.setEnabled(false);
		save.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doSave();
				}
			});
		bottom.add(save);
		
		open = new Button("Open");
		open.setEnabled(false);
		open.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doOpen();
				}
			});
		bottom.add(open);
		
		pack();
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		Insets insets = new Insets(i.top + 8, i.left + 8, i.bottom + 8, i.right + 8);
		return insets;
	}
	
	private void doChooseFile()
	{
		try
		{
			FileDialog d = new FileDialog(DocumentFileFrame.this, null, FileDialog.LOAD);
			d.show();
			if (d.getFile() != null)
				populate(new DocumentFile(d.getDirectory(), d.getFile()));
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
	
	private void doSave()
	{
		try
		{
			documentFile.setTitleAndExtension(title.getText(), extension.getText());
			documentFile.setMacCreatorAndType(macCreator.getText(), macType.getText());
			populate(documentFile);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
	
	private void doOpen()
	{
		try
		{
			if (!documentFile.open())
			{
				ApplicationDialog d = new ApplicationDialog(this,
					"Pick an application to open \"" + documentFile.getFile().getName() + "\"");
				d.show();
				if (d.getFile() != null)
					documentFile.openWith(new ApplicationFile(d.getDirectory(), d.getFile()));
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
	
	private void populate(DocumentFile documentFile) throws IOException
	{
		this.documentFile = documentFile;
		path.setText(documentFile.getPath());
		title.setEnabled(true);
		title.setText(documentFile.getTitle());
		extension.setEnabled(true);
		extension.setText(documentFile.getExtension());
		macCreator.setEnabled(true);
		macCreator.setText(documentFile.getMacCreator());
		macType.setEnabled(true);
		macType.setText(documentFile.getMacType());
		save.setEnabled(false);
		open.setEnabled(true);
	}
}
