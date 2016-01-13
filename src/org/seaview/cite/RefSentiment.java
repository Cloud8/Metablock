package org.seaview.cite;

import org.shanghai.crawl.MetaCrawl.Analyzer;

import org.seaview.cite.RefAnalyzer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;

/*
 * @author Götz Hatop
 * @title Citation Sentiment Analysis
 * @date 2016-01-03
 */
public class RefSentiment extends RefAnalyzer implements Analyzer {

    public interface Classifier {
        public Classifier create();
        public void dispose();
        public void probe();
        public String classify(String text);
        public void learn(String opinion, String text);
        public void save();
        public void load();
    }

    protected Property hasContext;
    protected Property hasCount;
    protected Property hasSentiment;
    private Classifier classifier;
    private HashMap<String,Integer> hash;
    private HashMap<String,Integer> data;
    private int count;

    public RefSentiment(Classifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public Analyzer create() {
        hash = new HashMap<String,Integer>();
        data = new HashMap<String,Integer>();
        classifier.create();
        log("created " + classifier.getClass().getSimpleName());
        return this;
    }

    @Override
    public void dispose() {
        hash.clear();
        data.clear();
        classifier.dispose();
        log("disposed " + probe());
    }

    @Override
    public String probe() {
        return super.probe() + " " + classifier.getClass().getSimpleName();
    }

    @Override
    public Resource analyze(Resource rc) {
        if (rc.getURI().startsWith("file://")) {
            test(rc.getURI().substring(7), false);
            return rc;
        } 
        Model model = rc.getModel();
        model.setNsPrefix("c4o", c4o);
        hasContext = model.createProperty(c4o, "hasContext");
        hasCount = model.createProperty(c4o, "hasInTextCitationFrequency");
        hasSentiment = model.createProperty(c4o, "hasSentiment");
        return super.analyze(rc); // analyzeReference
    }

    /**
     * determine polarity of citation context
     */
    @Override
    protected void analyzeReference(Resource rc, Resource reference) {
        if (reference.hasProperty(hasContext)) {
            // log("analyze: " + " " + reference.getURI());
        } else {
            log("no context: " + " " + reference.getURI());
        }
        StmtIterator sti = reference.listProperties(hasContext);
        while(sti.hasNext()) {
            Statement stmt = sti.nextStatement();
            if (!stmt.getObject().isLiteral()) {
                log("no text;"); 
                continue; 
            }
            String context = stmt.getString();
            if (reference.hasProperty(hasSentiment)) {
                //reference.removeAll(hasSentiment);
            }
            String sense = classifier.classify(context);
            if (!sense.equals("o")) log(sense + " " + context);
            //may be later : reference.addProperty(hasSentiment, sense);
        }
    }

    @Override
    public Resource test(Resource rc) {
        String fname = rc.getURI();
        if (fname.startsWith("file://")) {
            log("test " + fname.substring(7));
            //test(fname.substring(7), true); // train
            //test(fname.substring(7), false); // test
            test(fname.substring(7)); // train && test
        } 
        return rc;
    }

    /** learn from the first 80% of the data, use the last 20% for testing 
        for efficiency, separating test and training data is useful,
        this is for simplicity and to gain a rough understanding of
        training data.
     */
    private void test(String fname) {
        double percent = 0.85d;
        long count = 0L;
        try {
            count = Files.newBufferedReader(Paths.get(fname)).lines().count();
        } catch(IOException e) { log(e); }
        long mark = (long)(count * percent);
        log("test " + mark + " to " + count);
        test(fname, mark);
        classifier.save();
    }

    /** if learn, classifier should be trained */
    private void test(String fname, boolean learn) {
        if (learn) {
            classifier.probe();
            test(fname, 0);
        } else {
            classifier.load();
            test(fname, 1);
        }
        if (learn) {
            classifier.save();
        }
    }

    /** annotated data are separated by tabs 
        -- use with learn==true, mark==0 for training a classifier,
        -- use with learn==false, mark==0 for testing a classifier
        -- use mark for a separation of training and test data
      */
    private void test(String fname, long mark) {
        File file = new File(fname);
        int good=0, bad=0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line=br.readLine(); line!=null; line=br.readLine()) {
                if (line.startsWith("#") || line.length()==0) {
                    System.out.println(line);
                    continue;
                }
                String[] parts = line.split("\\t", 4);
                String opinion;
                String sentence;
                if (parts.length==2) { // polarity phrases
                    sentence = parts[0]; // sentence
                    opinion = parts[1].equals("1")?"p":"n";
                } else if (parts.length==4) { // annotated citation data
                    String id1 = parts[0];
                    String id2 = parts[1];
                    opinion = parts[2];
                    sentence = parts[3]; // sentence
                } else {
                    log("bad: [" + line + "] " + parts.length);
                    continue;
                }
                if (sentence.startsWith("\"")) {
                    sentence = sentence.substring(1);
                }
                if (sentence.endsWith("\"")) {
                    sentence = sentence.substring(0,sentence.length()-1);
                }
                if (mark==0) {
                    classifier.learn(opinion, sentence); // default
                } else if (count<mark) {
                    classifier.learn(opinion, sentence);
                } else {
                    String sentiment = classifier.classify(sentence);
                    if (remember(opinion, sentiment)) {
                        good++;
                    } else {
                        bad++;
                    }
                }
                count++;
            }
            if (mark>0) {
                double score = good * 100 / (count-mark);
                log("good: " + good + " bad: " + bad + " total " + (count-mark)
                             + "  / " + score + "%");
                log("classifier: " + hash.toString());
                log("test data: " + data.toString());
            }
        } catch(IOException e) { log(e); }
    }

    private boolean remember(String opinion, String sentiment) {
        boolean b = false;
        if (sentiment.equals(opinion)) {
            b = true;
        } else {
            if (sentiment.equals("n")) {
                // log(sentiment + " # " + opinion);
            } else if (sentiment.equals("o")) {
                // log(count + " " + sentiment + " # " + opinion);
            } else if (sentiment.equals("p")) {
                // log(count + " " + sentiment + " # " + opinion);
            } else {
                // log(count + " " + sentiment + " # " + opinion);
            }
        }
        if (hash.containsKey(sentiment)) {
             hash.put(sentiment, hash.get(sentiment)+1);
        } else {
             hash.put(sentiment, 1);
        }
        if (data.containsKey(opinion)) {
             data.put(opinion, data.get(opinion)+1);
        } else {
             data.put(opinion, 1);
        }
        return b;
    }

    private static final Logger logger =
                         Logger.getLogger(RefSentiment.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

}
