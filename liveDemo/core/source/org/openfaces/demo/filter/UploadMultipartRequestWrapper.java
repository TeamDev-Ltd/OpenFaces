/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2011, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

package org.openfaces.demo.filter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class UploadMultipartRequestWrapper extends HttpServletRequestWrapper {

    public UploadMultipartRequestWrapper(HttpServletRequest request, String tempDirPath, final long maxSizeOfFile) {
        super(request);
        final String contentLength = request.getHeader("content-length");
        if (contentLength == null)
            return;

        final long requestLength = Long.parseLong(contentLength.trim());
        if (requestLength > maxSizeOfFile)
            return;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setFileItemFactory(new ProgressMonitorFileItemFactory(request));
            List<FileItem> fileItems = upload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                if (!fileItem.isFormField()) {
                    File f = writeFile(fileItem, tempDirPath);
                    request.setAttribute(fileItem.getFieldName(), f);
                    break;
                }
            }

        } catch (FileUploadException fe) {
            //Request Timed out.
            //Do some logging
            throw new RuntimeException(fe);
        } catch (Exception ne) {
            throw new RuntimeException(ne);
        }
    }

    private File writeFile(FileItem fileItem, String tempDirPath) throws Exception {
        File f = getAndChangeFileNameIfNeeded(tempDirPath, fileItem.getName());

        OutputStream out = new FileOutputStream(f);
        int read = 0;
        byte[] bytes = new byte[1024];
        InputStream inputStream = fileItem.getInputStream();
        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        inputStream.close();
        out.flush();
        out.close();
        return f;
    }

    private File getAndChangeFileNameIfNeeded(String tempDirPath, String fileName) {
        int indexOfSlash = fileName.lastIndexOf("\\");
        fileName = (indexOfSlash == -1) ? fileName : fileName.substring(indexOfSlash + 1);
        File f = new File(tempDirPath + "\\" + fileName);
        int i = 0;
        while (f.isFile()) {
            f = new File(tempDirPath + "\\copy_" + i + "_" + fileName);
            i++;
        }
        return f;
    }

}