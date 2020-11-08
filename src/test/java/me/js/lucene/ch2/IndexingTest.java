package me.js.lucene.ch2;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexingTest {

    public static final String INDEX_PATH = "/Users/82109/IdeaProjects/lucene/src/main/resources/file";

    protected String[] ids = {"1", "2"};
    protected String[] unindexed = {"Netherlands", "Italy"};
    protected String[] unstored = {"Amsterdam has lots of bridges",
            "Venice has lots of canals"};
    protected String[] text = {"Amsterdam", "Venice"};
    private Directory directory;

    @BeforeEach
    void setUp() throws IOException {
        // file 폴더 하위 삭제 후, 인덱싱
        File deleteFolder = new File(INDEX_PATH);
        File[] deleteFolderList = deleteFolder.listFiles();
        for (int j = 0; j < deleteFolderList.length; j++) deleteFolderList[j].delete();

        directory = FSDirectory.open(Paths.get(INDEX_PATH));
        IndexWriter writer = getWriter();

        for (int i = 0; i < ids.length; i++) {      //3
            Document doc = new Document();
            doc.add(new Field("id", ids[i], storedNoTokenizedType()));
            doc.add(new Field("country", unindexed[i], storedTokenizedType()));
            doc.add(new TextField("contents", unstored[i], Field.Store.NO));
            doc.add(new TextField("city", text[i], Field.Store.YES));
            writer.addDocument(doc);
        }
        writer.close();
    }

    @Test
    @DisplayName("색인 maxDoc 테스트 ")
    void searchTest() throws IOException {
        IndexSearcher searcher = getIndexSearcher();
        IndexReader indexReader = searcher.getIndexReader();
        assertEquals(ids.length,indexReader.maxDoc());
    }

    @Test
    @DisplayName("간단한 단일 텀 질의 생성")
    void simpleQuery() throws IOException {
        IndexSearcher searcher = getIndexSearcher();
        Term t = new Term("city", "Amsterdam");
        Query query = new TermQuery(t);
        TopDocs foundDocs = searcher.search(query, 1);
        System.out.println(foundDocs.totalHits);
        for (ScoreDoc sd : foundDocs.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            System.out.println("Doc : " + sd.doc + " :: " + d.get("city"));
        }

    }


    private IndexSearcher getIndexSearcher() throws IOException {
        FSDirectory directory = FSDirectory.open(Paths.get(INDEX_PATH));
        DirectoryReader reader = DirectoryReader.open(directory);
        return new IndexSearcher(reader);
    }
    private FieldType storedTokenizedType() {
        FieldType storedTokenizedType = new FieldType();
        storedTokenizedType.setStored(true);
        storedTokenizedType.setTokenized(true);
        return storedTokenizedType;
    }

    private FieldType storedNoTokenizedType() {
        FieldType storedTokenizedType = new FieldType();
        storedTokenizedType.setStored(true);
        storedTokenizedType.setTokenized(false);
        return storedTokenizedType;
    }

    private IndexWriter getWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        return new IndexWriter(directory, config);
    }
}