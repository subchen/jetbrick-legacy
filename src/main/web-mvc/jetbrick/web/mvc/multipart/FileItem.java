package jetbrick.web.mvc.multipart;

import java.io.*;
import org.apache.commons.io.*;

public class FileItem {
	protected final String fieldName;
	protected final String originalFileName;
	protected final String originalFileExt;
	protected final File diskFile;
	protected final long size;

	public FileItem(String fieldName, String originalFileName, File diskFile) {
		this.fieldName = fieldName;
		this.originalFileName = originalFileName;
		this.originalFileExt = FilenameUtils.getExtension(originalFileName);
		this.diskFile = diskFile;
		this.size = diskFile.length();
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getOriginalFileExt() {
		return originalFileExt;
	}

	public File getDiskFile() {
		return diskFile;
	}

	public long getSize() {
		return size;
	}

	public void delete() {
		if (diskFile.exists()) {
			diskFile.delete();
		}
	}

	public void moveTo(File destFile) {
		try {
			FileUtils.moveFile(diskFile, destFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(File destFile) {
		try {
			FileUtils.copyFile(diskFile, destFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(OutputStream os) {
		try {
			IOUtils.copy(new FileInputStream(diskFile), os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public InputStream asInputStream() {
		try {
			return new FileInputStream(diskFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public String asString(String charset) {
		try {
			return FileUtils.readFileToString(diskFile, charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}