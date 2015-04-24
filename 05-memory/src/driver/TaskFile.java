package driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

public class TaskFile {
	//Default location for the trace files: project's working directory. Change if needed.
	static private final String dirPath = "."; 
	
	private String display;
	private File file;
	
	//A TaskFile represents a ("display name", "file name") pair, to be used in the tasks dialog.
	public TaskFile(String display, String filepath) throws FileNotFoundException {
		this.display = display;
		if (filepath == null)
			return; //File-less instance (no task)
		
		file = new File(filepath);
		if (!file.exists()) {
			String path = dirPath+File.separator+filepath;
			file = new File(path);
			if (!file.exists())
				throw new FileNotFoundException(path);
		}
	}
	
	//Returned string is displayed as combo-box text
	public String toString() {
		return display;
	}
	
	public File getFile() {
		return file;
	}
	
	//The default list for our simulator
	public static Vector<TaskFile> getDefaults() {
		Vector<TaskFile> v = new Vector<TaskFile>(5);
		try {
			v.add(new TaskFile("None", null));
			v.add(new TaskFile("ls \"/\"", "lstrace.out"));
			v.add(new TaskFile("echo \"hello world\"", "echotrace.out"));
			v.add(new TaskFile("touch \"hello.world\"", "touchtrace.out"));
			v.add(new TaskFile("bc \"2+2\"", "bctrace.out"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File "+e.getMessage()+" not found. Check that the trace files were unpacked and placed in the project's root directory.");
			System.exit(1);
		}
		return v;
	}
}
