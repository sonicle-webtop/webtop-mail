/*
* WebTop Services is a web application framework developed by Sonicle S.r.l.
* Copyright (C) 2011 Sonicle S.r.l.
*
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU Affero General Public License version 3 as published by
* the Free Software Foundation with the addition of the following permission
* added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
* WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
* WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program; if not, see http://www.gnu.org/licenses or write to
* the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
* MA 02110-1301 USA.
*
* You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
*
* The interactive user interfaces in modified source and object code versions
* of this program must display Appropriate Legal Notices, as required under
* Section 5 of the GNU Affero General Public License version 3.
*
* In accordance with Section 7(b) of the GNU Affero General Public License
* version 3, these Appropriate Legal Notices must retain the display of the
* "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
* feasible for technical reasons, the Appropriate Legal Notices must display
* the words "Powered by Sonicle WebTop".
*/

package com.sonicle.webtop.mail;

import java.io.File;
import org.apache.commons.vfs2.FileObject;



/**

 * This class represents an element in a multipart request.

 * It has a few methods for determining

 * whether or not the element is a String or a file,

 * and methods to retrieve the data of the aforementioned

 * element.  Text input elements have a <code>null</code> content type,

 * files have a non-null content type.

 *

 * @author Mike Schachter

 */

public class MultipartElement {



    /**

     * The content type of this element

     */

    protected String contentType;





    /**

     * The element data

     */

    protected byte[] eldata;



    /**

     * The element's data represented in a (possibly temporary) file

     */

    protected File file;



    /**

     * The element's data represented in a VFS FileObject

     */

    protected FileObject fileObject;



    /**

     * The element name

     */

    protected String name;



    /**

     * The element's filename, null for text elements

     */

    protected String fileName;



    /**

     * The element's text value, null for file elements

     */

    protected String value;



    /**

     * Whether or not this element is a file

     */

    protected boolean isFile = false;



    /**

     * @deprecated Use the constructor that takes an File as an argument

     *             as opposed to a byte array argument, which can cause

     *             memory problems

     */

    public MultipartElement(String name, String fileName, String contentType, byte[] eldata) {

        this.name = name;

        this.fileName = fileName;

        this.contentType = contentType;

        this.eldata = eldata;



        if (fileName != null) {

            isFile = true;

        }

    }



    /**

     * Constructor for a file element

     * @param name The form name of the element

     * @param fileName The file name of the element if this element is a file

     * @param contentType The content type of the element if a file

     * @param file The (possibly temporary) file representing this element if

     *             it's a file

     */

    public MultipartElement(String name,

                            String fileName,

                            String contentType,

                            File file) {



        this.name = name;

        this.fileName = fileName;

        this.contentType = contentType;

        this.file = file;

        this.isFile = true;

    }


    public MultipartElement(String name,

                            String fileName,

                            String contentType,

                            FileObject file) {



        this.name = name;

        this.fileName = fileName;

        this.contentType = contentType;

        this.fileObject = file;

        this.isFile = true;

    }

    /**

     * Constructor for a text element

     * @param name The name of the element

     * @param value The value of the element

     */

    public MultipartElement(String name, String value) {

        this.name = name;

        this.value = value;

        this.isFile = false;

    }





    /**

     * Retrieve the content type

     */

    public String getContentType() {

        return contentType;

    }





    /**

     * Retrieve the data

     * @deprecated Use the getFile method to get a File representing the

     *             data for this element

     */

    public byte[] getData() {

        return eldata;

    }



    /**

     * Get the File that holds the data for this element.

     */

    public File getFile() {

        return file;

    }


    /**

     * Get the File that holds the data for this element.

     */

    public FileObject getFileObject() {

        return fileObject;

    }

    /**

     * Retrieve the name

     */

    public String getName() {

        return name;

    }



    /**

     * Retrieve the filename, can return <code>null</code>

     * for text elements

     */

    public String getFileName() {

        return fileName;

    }



    /**

     * Returns the value of this multipart element

     * @return A String if the element is a text element, <code>null</code>

     *         otherwise

     */

    public String getValue() {

        return value;

    }



    /**

     * Set the file that represents this element

     */

    public void setFile(File file) {

        this.file = file;

    }



    /**

     * Set the file name for this element

     */

    public void setFileName(String fileName) {

        this.fileName = fileName;

    }



    /**

     * Set the name for this element

     */

    public void setName(String name) {

        this.name = name;

    }



    /**

     * Set the content type

     */

    public void setContentType(String contentType) {

        this.contentType = contentType;

    }



    /**

     * Is this element a file?

     */

    public boolean isFile() {

        if (file == null) {

            return false;

        }

        return true;

    }

    /**

     * Is this element a file?

     */

    public boolean isFileObject() {

        if (fileObject == null) {

            return false;

        }

        return true;

    }


    public void setValue(String value) {

        this.value = value;

    }



    /**

     * Set the data

     * @deprecated Use the setFile method to set the file

     *             that represents the data of this element

     */

    public void setData(byte[] eldata) {

        this.eldata = eldata;

    }

}



