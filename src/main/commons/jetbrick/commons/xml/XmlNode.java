package jetbrick.commons.xml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import jetbrick.commons.exception.SystemException;
import org.dom4j.*;
import org.dom4j.io.*;

@SuppressWarnings("unchecked")
public class XmlNode {

    private final Element node;

    /**
     * Read a xml document and get root element.
     * 
     * @param file
     */
    public static XmlNode create(File file) {
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(file);
            return transform(doc.getRootElement());
        } catch (DocumentException e) {
            throw SystemException.unchecked(e);
        }
    }

    /**
     * Read a xml document and get root element.
     */
    public static XmlNode create(InputStream is) {
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(is);
            return transform(doc.getRootElement());
        } catch (DocumentException e) {
            throw SystemException.unchecked(e);
        }
    }

    /**
     * Create a xml node with name.
     */
    public static XmlNode create(String tagName) {
        return transform(DocumentHelper.createElement(tagName));
    }

    /**
     * Create a xml node with name and namespace.
     */
    public static XmlNode create(String tagName, String namespaceURI) {
        return transform(DocumentHelper.createElement(new QName(tagName, new Namespace("", namespaceURI))));
    }

    /**
     * Parses the given text as an XML document and returns the newly created root node.
     */
    public static XmlNode parseXml(String xml) {
        try {
            Document document = DocumentHelper.parseText(xml);
            return transform(document.getRootElement());
        } catch (DocumentException e) {
            throw SystemException.unchecked(e);
        }
    }

    private static XmlNode transform(Element node) {
        if (node == null) {
            return null;
        }
        return new XmlNode(node);
    }

    private static List<XmlNode> transformList(List<Element> nodes) {
        List<XmlNode> results = new ArrayList<XmlNode>(nodes.size());
        for (Element element : nodes) {
            results.add(transform(element));
        }
        return results;
    }

    private XmlNode(Element node) {
        this.node = node;
    }

    public String name() {
        return node.getName();
    }

    public void name(String value) {
        node.setName(value);
    }

    public String text() {
        return node.getTextTrim();
    }

    public void text(String value) {
        node.setText(value);
    }

    /**
     * Get a unique xpath expression.
     */
    public String path() {
        return node.getUniquePath();
    }

    public XmlNode parent() {
        return transform(node.getParent());
    }

    public XmlNode root() {
        return transform(node.getDocument().getRootElement());
    }

    public boolean isRoot() {
        return node.isRootElement();
    }

    /**
     * @return the new added node.
     */
    public XmlNode add(String tagName) {
        return transform(node.addElement(tagName));
    }

    /**
     * Add a child with namespace
     * @return the new added node.
     */
    public XmlNode add(String tagName, String namespaceURI) {
        return transform(node.addElement(tagName, namespaceURI));
    }

    /**
     * Add child node
     * @return child node
     */
    public XmlNode add(XmlNode child) {
        node.add(child.node);
        return child;
    }

    /**
     * Remove node from parent
     */
    public void remove() {
        if (node.getParent() != null) {
            node.getParent().remove(node);
        }
    }

    public XmlNode element(String tagName) {
        return transform(node.element(tagName));
    }

    /**
     * Equals element(tagName).getText().
     */
    public String elementText(String tagName) {
        return node.elementTextTrim(tagName);
    }

    public List<XmlNode> elements() {
        return transformList(node.elements());
    }

    public List<XmlNode> elements(String tagName) {
        return transformList(node.elements(tagName));
    }

    public XmlNode selectSingleNode(String xpath) {
        return transform((Element) node.selectSingleNode(xpath));
    }

    public List<XmlNode> selectNodes(String xpath) {
        return transformList(node.selectNodes(xpath));
    }

    public String attributeValue(String name) {
        return node.attributeValue(name);
    }

    public List<XmlAttribute> attributes() {
        return XmlAttribute.transformList(node.attributes());
    }

    public XmlAttribute attribute(String name) {
        return new XmlAttribute(node.attribute(name));
    }

    /**
     * Set the attribute value of the given name.<br/>
     * If value is null, the attribute will be removed.
     * 
     * @param name
     * @param value
     */
    public XmlNode attribute(String name, Object value) {
        if (value == null) {
            node.addAttribute(name, null);
        } else {
            node.addAttribute(name, value.toString());
        }
        return this;
    }

    public String asXml() {
        return node.asXML();
    }

    public String asFormatedXml() {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            Writer out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(node);
            writer.close();
            return out.toString();
        } catch (IOException e) {
            throw SystemException.unchecked(e);
        }
    }

    public void write(OutputStream out, String encoding) {
        try {
            Document document = DocumentHelper.createDocument(node);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(encoding);
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
        } catch (IOException e) {
            throw SystemException.unchecked(e);
        }
    }

    public static void main(String[] args) {
        XmlNode xml = XmlNode.create("schema");
        xml.add("table").attribute("file", "aaa");
        xml.add("table").attribute("file", "aaa");
        xml.write(System.out, "utf-8");
    }
}
