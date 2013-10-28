package jetbrick.web.mvc.view;

import java.io.*;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;

public class DefaultViewRender extends AbstractViewRender {

    public DefaultViewRender() {
        viewPattern = "*.jsp";
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        HttpServletRequest request = rc.getRequest();

        for (Map.Entry<String, Object> attr : rc.getAttributes().entrySet()) {
            request.setAttribute(attr.getKey(), attr.getValue());
        }

        String layoutView = (String) request.getAttribute(layoutKey);
        if (layoutView == null) {
            // no layout 
            doRender(rc, view);
        } else {
            // output main content 
            CachedResponse response = new CachedResponse(rc.getResponse(), encoding);
            request.getRequestDispatcher(view).include(request, response);
            String mainContent = response.getContent();
            request.setAttribute(mainContentKey, mainContent);

            // output layout 
            doRender(rc, layoutView);
        }
    }

    protected void doRender(RequestContext rc, String view) throws Throwable {
        if (view.endsWith(".jsp")) {
            String file = rc.getServletContext().getRealPath(view);
            if (!new File(file).exists()) {
                throw new SystemException(HttpError.STATUS_404, "jsp not found: " + view);
            }
        }
        rc.getRequest().getRequestDispatcher(view).forward(rc.getRequest(), rc.getResponse());
    }

    static class CachedResponse extends HttpServletResponseWrapper {
        private ByteArrayOutputStream os;
        private PrintWriter writer;
        private ByteArrayServletOutputStream output;
        private String encoding;

        public CachedResponse(HttpServletResponse response, String encoding) {
            super(response);

            this.os = new ByteArrayOutputStream(4096);
            this.encoding = encoding;
        }

        @Override
        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(os, encoding));
            }
            return writer;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (output == null) {
                output = new ByteArrayServletOutputStream(os);
            }
            return output;
        }

        public String getContent() {
            try {
                return os.toString(encoding);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() throws IOException {
            os.close();
            if (writer != null) {
                writer.close();
            }
            if (output != null) {
                output.close();
            }
        }

    }

    static class ByteArrayServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream os;

        public ByteArrayServletOutputStream(ByteArrayOutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int i) throws IOException {
            os.write(i);
        }
    }
}
