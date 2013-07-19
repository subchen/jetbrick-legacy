package jetbrick.web.mvc.multipart;

import java.io.*;
import java.net.URLDecoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class FileUploaderUtils {

	public static boolean supported(HttpServletRequest request) {
		String originalFilename = request.getHeader("content-disposition");
		if (originalFilename != null) return true;

		return ServletFileUpload.isMultipartContent(request);
	}

	public static HttpServletRequest asRequest(HttpServletRequest request, File uploadSavePath) {
		try {
			String originalFilename = request.getHeader("content-disposition");
			if (originalFilename != null) {
				return asHtml5Request(request, uploadSavePath);
			} else if (ServletFileUpload.isMultipartContent(request)) {
				return asMultipartRequest(request, uploadSavePath);
			}
			return request;
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	// multipart/form-data
	private static FileUploaderRequest asMultipartRequest(HttpServletRequest request, File uploadSavePath) throws Throwable {
		FileUploaderRequest req = new FileUploaderRequest(request);

		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator it = upload.getItemIterator(request);
		while (it.hasNext()) {
			FileItemStream item = it.next();
			String fieldName = item.getFieldName();
			InputStream stream = item.openStream();
			if (item.isFormField()) {
				req.setParameter(fieldName, Streams.asString(stream));
			} else {
				String originalFilename = item.getName();
				File diskFile = new File(uploadSavePath, RandomStringUtils.randomAlphanumeric(16));

				OutputStream fos = new FileOutputStream(diskFile);
				IOUtils.copy(stream, fos);
				IOUtils.closeQuietly(fos);

				FileItem fileItem = new FileItem(fieldName, originalFilename, diskFile);
				req.setFile(fileItem.getFieldName(), fileItem);
			}
			IOUtils.closeQuietly(stream);
		}

		return req;
	}

	// application/octet-stream
	private static FileUploaderRequest asHtml5Request(HttpServletRequest request, File uploadSavePath) throws Throwable {
		String originalFilename = request.getHeader("content-disposition");
		if (originalFilename == null) {
			throw new ServletException("The request is not a html 5 file upload request.");
		}
		originalFilename = new String(originalFilename.getBytes("iso8859-1"), request.getCharacterEncoding());
		originalFilename = StringUtils.substringAfter(originalFilename, "; filename=");
		originalFilename = StringUtils.remove(originalFilename, "\"");
		originalFilename = URLDecoder.decode(originalFilename, "utf-8");

		File diskFile = new File(uploadSavePath, RandomStringUtils.randomAlphanumeric(16));

		InputStream fis = request.getInputStream();
		OutputStream fos = new FileOutputStream(diskFile);
		IOUtils.copy(fis, fos);
		IOUtils.closeQuietly(fis);
		IOUtils.closeQuietly(fos);

		FileUploaderRequest req = new FileUploaderRequest(request);
		FileItem fileItem = new FileItem("unknown", originalFilename, diskFile);
		req.setFile(fileItem.getFieldName(), fileItem);
		return req;
	}
}