package geobe.learn;

import org.apache.xerces.parsers.SAXParser;
import de.geobe.tubecut.calc.SvgGenerator;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DocumentCreator {
    public static void main(String[] args) throws ParserConfigurationException, SAXException {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory("org.apache.xerces.parsers.SAXParser");
        String svg = (String) SvgGenerator.calculateTubecutPlot();
        InputStream stream = new ByteArrayInputStream(svg.getBytes());
        try {
            SVGDocument doc = factory.createSVGDocument("memory://tubecut.svg", stream);
            System.out.println(doc.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
