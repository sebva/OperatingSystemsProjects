package driver;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import provided.MMU_Interface;
import provided.PhysicalMemory;

public class GUI extends JFrame {

	private JSpinner taskSize[];
	private JTextField tasksSize;
	private JSpinner quantumSize;

	private JComboBox task[];
	private JTextArea log;

	private int numTasks;
	private static final long serialVersionUID = 1L;
	DateFormat dateFormat;

	public GUI(int numTasks) {
		super();
		this.numTasks = numTasks;
		taskSize = new JSpinner[numTasks];
		task = new JComboBox[numTasks];
		constructUI();
		updateSize();
		setTitle("Memory Simulator");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		dateFormat = new SimpleDateFormat("HH:mm:ss");
	}

	private void constructUI() {
		JTextField text;
		JSpinner spinner;
		Vector<TaskFile> tasks = TaskFile.getDefaults();
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateSize();
			}
		};
		ChangeListener cl = new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateSize();
			}
		};
		Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 2, 2);
		pane.add(new JLabel("Memory Size:", JLabel.LEFT), c);

		c.gridx++;
		c.weightx = 1.0;
		text = new JTextField();
		text.setText(String.valueOf(PhysicalMemory.MEMORY_SIZE));
		text.setEditable(false);
		text.setHorizontalAlignment(JTextField.RIGHT);
		pane.add(text, c);
		c.weightx = 0.0;

		c.gridx++;
		pane.add(new JLabel("bytes", JLabel.LEFT), c);

		c.gridx++;
		c.gridwidth = 3;
		c.gridheight = 2;
		JButton b = new JButton("Start Simulation");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				startSimulation();
			}
		});
		pane.add(b, c);

		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		pane.add(new JLabel("Page Size:", JLabel.LEFT), c);

		c.gridx++;
		text = new JTextField();
		text.setText(String.valueOf(MMU_Interface.PAGE_SIZE));
		text.setEditable(false);
		text.setHorizontalAlignment(JTextField.RIGHT);
		pane.add(text, c);

		c.gridx++;
		pane.add(new JLabel("bytes", JLabel.LEFT), c);

		for (int i = 0; i < numTasks; i++) {
			JComboBox combo;

			c.gridx = 0;
			c.gridy++;
			pane.add(new JLabel("Task " + String.valueOf(i + 1) + ":",
					JLabel.LEFT), c);

			c.gridx++;
			c.gridwidth = 2;
			c.weightx = 2.0;
			combo = new JComboBox(tasks);
			combo.setEditable(false);
			combo.addActionListener(al);
			pane.add(combo, c);
			c.weightx = 1.0;
			task[i] = combo;

			c.gridx += 2;
			c.gridwidth = 1;
			pane.add(new JLabel("Size:", JLabel.LEFT), c);

			c.gridx++;
			c.weightx = 1.0;
			spinner = new JSpinner(new SpinnerNumberModel(1024, 8,
					PhysicalMemory.MEMORY_SIZE, 8));
			spinner.addChangeListener(cl);
			pane.add(spinner, c);
			c.weightx = 0.0;
			taskSize[i] = spinner;

			c.gridx++;
			pane.add(new JLabel("bytes", JLabel.LEFT), c);
		}

		c.gridx = 0;
		c.gridy++;
		pane.add(new JLabel("Quantum Size:", JLabel.LEFT), c);

		c.gridx++;
		spinner = new JSpinner(new SpinnerNumberModel(100, 1, 999999, 10));
		pane.add(spinner, c);
		quantumSize = spinner;

		c.gridx++;
		pane.add(new JLabel("ops", JLabel.LEFT), c);

		c.gridx++;
		pane.add(new JLabel("Total:", JLabel.LEFT), c);

		c.gridx++;
		text = new JTextField();
		text.setEditable(false);
		text.setHorizontalAlignment(JTextField.RIGHT);
		pane.add(text, c);
		tasksSize = text;

		c.gridx++;
		pane.add(new JLabel("bytes", JLabel.LEFT), c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 6;
		c.weighty = 1.0;
		log = new JTextArea(10, 40);
		log.setEditable(false);
		log.setLineWrap(true);
		log.setWrapStyleWord(true);
		pane.add(new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);
	}

	public void timeStamp() {
		Date d = new Date();
		log.append("\n"+dateFormat.format(d) + "\n");
	}

	public void message(String string) {
		log.append(string + "\n");
	}

	private TaskFile getTask(int index) {
		return (TaskFile) task[index].getSelectedItem();
	}

	private int getTaskSize(int index) {
		return ((SpinnerNumberModel) taskSize[index].getModel()).getNumber()
				.intValue();
	}

	private int getQuantumSize() {
		return ((SpinnerNumberModel) quantumSize.getModel()).getNumber()
				.intValue();
	}

	public void updateSize() {
		int size = 0;
		for (int i = 0; i < numTasks; i++) {
			TaskFile current = getTask(i);
			if (current.getFile() != null) {
				size += getTaskSize(i);
			}
		}
		tasksSize.setText(String.valueOf(size));
	}

	public void startSimulation() {

		final GUI g = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Vector<Task> tasks = new Vector<Task>(numTasks);
				TaskFile tf;
				File file;
				for (int i = 0; i < numTasks; i++) {
					tf = getTask(i);
					file = tf.getFile();
					if (file != null) {
						try {
							tasks.add(new Task(file, getTaskSize(i)));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							message("Failed to add task #"
									+ String.valueOf(i + 1) + ": "
									+ tf.toString());
							return;
						}
					}
				}
				if (tasks.size() == 0) {
					message("No tasks selected.");
					return;
				}
				Driver.runSimulation(g, tasks, getQuantumSize());
			}
		});
	}

	
	//TEST UNIT for the GUI
	public static void main(String[] args) {
		GUI i = new GUI(4);
		i.pack();
		i.setVisible(true);
	}
}
