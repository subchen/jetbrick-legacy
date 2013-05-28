package jetbrick.commons.xml;

import java.util.ArrayList;
import java.util.List;
import jetbrick.commons.bean.conv.DataConverter;
import org.dom4j.Attribute;

public class XmlAttribute extends DataConverter {
    private final Attribute attr;

    protected static List<XmlAttribute> transformList(List<Attribute> attrs) {
        List<XmlAttribute> results = new ArrayList<XmlAttribute>(attrs.size());
        for (Attribute attr : attrs) {
            results.add(new XmlAttribute(attr));
        }
        return results;
    }

    protected XmlAttribute(Attribute attr) {
        this.attr = attr;
    }

    @Override
    public boolean exist() {
        return attr != null;
    }

    public String name() {
        return exist() ? attr.getName() : null;
    }

    @Override
    public String value() {
        return exist() ? attr.getValue() : null;
    }

    @Override
    public String[] values() {
        return exist() ? new String[] { attr.getValue() } : null;
    }
}
