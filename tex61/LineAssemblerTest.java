package tex61;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** Testing for the LineAssembler class. The InputParser and Controller
 * are better tested in blackbox testing.
 * @author Jason Qiu
 *
 */
public class LineAssemblerTest {

    @Test
    public void test1() {
        testLines = new ArrayList<String>();
        testLines.add("   hello");
        outList = new ArrayList<String>();
        pages = new PageCollector(outList);
        LineAssembler lineAssembler = new LineAssembler(pages);
        lineAssembler.addText("hello");
        lineAssembler.finishWord();
        lineAssembler.endParagraph();
        assertEquals("error in addText, finishWord, or endParagraph"
                , testLines, outList);
    }

    @Test
    public void test2() {
        testLines = new ArrayList<String>();
        testLines.add("hello");
        outList = new ArrayList<String>();
        pages = new PageCollector(outList);
        LineAssembler lineAssembler = new LineAssembler(pages);
        lineAssembler.setParIndentation(0);
        lineAssembler.addText("hello");
        lineAssembler.finishWord();
        lineAssembler.endParagraph();
        assertEquals("error in setParIndentation"
                , testLines, outList);
    }

    @Test
    public void test3() {
        testLines = new ArrayList<String>();
        testLines.add("hello");
        outList = new ArrayList<String>();
        pages = new PageCollector(outList);
        LineAssembler lineAssembler = new LineAssembler(pages);
        lineAssembler.setParIndentation(-50);
        lineAssembler.setIndentation(50);
        lineAssembler.addText("hello");
        lineAssembler.finishWord();
        lineAssembler.endParagraph();
        assertEquals("error in setParIndentation or setIndentation"
                , testLines, outList);
    }

    @Test
    public void test4() {
        testLines = new ArrayList<String>();
        testLines.add("hello");
        testLines.add("\fworld");
        outList = new ArrayList<String>();
        pages = new PageCollector(outList);
        LineAssembler lineAssembler = new LineAssembler(pages);
        lineAssembler.setParIndentation(0);
        lineAssembler.setTextWidth(1);
        lineAssembler.setTextHeight(1);
        lineAssembler.addText("hello");
        lineAssembler.finishWord();
        lineAssembler.addText("world");
        lineAssembler.finishWord();
        lineAssembler.endParagraph();
        assertEquals("error in setTextWidth or setTextHeight"
                , testLines, outList);
    }

    @Test
    public void test5() {
        outList = new ArrayList<String>();
        pages = new PageCollector(outList);
        LineAssembler lineAssembler = new LineAssembler(pages);
        lineAssembler.addText("hello");
        lineAssembler.finishWord();
        lineAssembler.newLine();
        assertTrue("newLine should have no effect", outList.isEmpty());
    }

    /** Lines of test data. */
    private List<String> testLines;
    /** Lines from a PageCollector. */
    private List<String> outList;
    /** Target PageAssembler. */
    private PageAssembler pages;
}
