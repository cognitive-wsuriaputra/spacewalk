/**
 * Copyright (c) 2008 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * 
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation. 
 */
package com.redhat.rhn.taskomatic.task.repomd;

import java.io.IOException;
import java.io.Writer;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.SAXException;
/**
 * 
 * @version $Rev $
 *
 */
public class RepomdIndexWriter {
	
    private SimpleContentHandler handler;

    private RepomdIndexData primary;
    private RepomdIndexData filelists;
    private RepomdIndexData other;
    private RepomdIndexData updateinfo;
    private RepomdIndexData group;

    /**
     * 
     * @param writer content writer
     * @param primary primary.xml data
     * @param filelists filelists.xml data
     * @param other other.xml data
     * @param updateinfo updateinfo.xml data
     * @param group group data
     */
    public RepomdIndexWriter(Writer writer, RepomdIndexData primary, 
            RepomdIndexData filelists, RepomdIndexData other, 
            RepomdIndexData updateinfo, RepomdIndexData group) {

        this.primary = primary;
        this.filelists = filelists;
        this.other = other;
        this.updateinfo = updateinfo;
        this.group = group;

        OutputFormat of = new OutputFormat();

        XMLSerializer serializer = new XMLSerializer(writer, of);

        try {
            handler = new SimpleContentHandler(serializer.asContentHandler());
        } 
        catch (IOException e) {
            // XXX fatal error
        }
        try {
            handler.startDocument();
        } 
        catch (SAXException e) {
            // XXX fatal error
        }
    }
    /**
     * writes the repomd index
     */
    public void writeRepomdIndex() {
        begin();
        writeData("primary", primary);
        writeData("filelists", filelists);
        writeData("other", other);

        // updateinfo is optional (channels with no errata)
        if (updateinfo != null) {
            writeData("updateinfo", updateinfo);
        }

        // likewise for group info
        if (group != null) {
            writeData("group", group);
        }

        end();
    }
    /**
     * Writes the data to xml
     * @param type
     * @param data Repomd index data
     */
    private void writeData(String type, RepomdIndexData data) {
        SimpleAttributesImpl attr = new SimpleAttributesImpl();
        attr.addAttribute("type", type);

        String location = type + ".xml.gz";
        //special case for comps file
        if (type.equals("group")) {
            location = "comps.xml";
        }

        try {
            handler.startElement("data", attr);

            attr.clear();
            attr.addAttribute("href", "repodata/" + location);
            handler.startElement("location", attr);
            handler.endElement("location");

            attr.clear();
            attr.addAttribute("type", "sha");
            handler.startElement("checksum", attr);
            handler.addCharacters(data.getChecksum());
            handler.endElement("checksum");

            // this can be null for group info, since it is uncompressed
            if (data.getOpenChecksum() != null) {
                attr.clear();
                attr.addAttribute("type", "sha");
                handler.startElement("open-checksum", attr);
                handler.addCharacters(data.getOpenChecksum());
                handler.endElement("open-checksum");
            }

            handler.addElementWithCharacters("timestamp", Long.toString(data.getTimestamp()
                                                                       .getTime() / 1000));

            handler.endElement("data");
        } 
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }
    /**
     * begin the xml creation
     */
    private void begin() {
        SimpleAttributesImpl attr = new SimpleAttributesImpl();
        attr.addAttribute("xmlns", "http://linux.duke.edu/metadata/repo");
        try {
            handler.startElement("repomd", attr);
        } 
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }
    /**
     * End xml creation
     */
    private void end() {
        try {
            handler.endElement("repomd");
            handler.endDocument();
        } 
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

}
