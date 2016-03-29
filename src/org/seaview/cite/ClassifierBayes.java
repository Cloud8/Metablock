package org.seaview.cite;

import org.shanghai.util.FileUtil;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.util.Arrays;

import org.seaview.bayes.BayesClassifier;
import org.seaview.bayes.Classifier;

/**
   @title Naive Bayes Sentiment analysis
   @date 2016-01-03
 */
public class ClassifierBayes implements RefSentiment.Classifier {

    private String modelFile;
    private int count;
    private Classifier<String, String> bayes;

    public ClassifierBayes(String modelFile) {
        this.modelFile = modelFile;
    }

    @Override
    public RefSentiment.Classifier create() {
        count = 0;
        bayes = new BayesClassifier<String, String>();
        bayes.learn("o", Arrays.asList("the a".split("\\s"))); // corrigee 
        return this;
    }

    @Override
    public void dispose() {
        log("disposed " + this.getClass().getSimpleName());
    }

    @Override
    public void probe() {
        log(this.getClass().getSimpleName());
    }

    @Override
    public void load() {
        bayes = load(modelFile);
    }

    @Override
    public void save() {
        try {
            FileUtil.write(modelFile, serialize(bayes));
            log("wrote " + modelFile);
        } catch(IOException e) { log(e); }
    }

    @Override
    public void learn(String opinion, String sentence) {
        bayes.learn(opinion, Arrays.asList(sentence.split("\\s")));
    }

    @Override
    public String classify(String text) {
        return bayes.classify(Arrays.asList(text.split("\\s"))).getCategory();
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private Classifier<String, String> load(String fname) { 
        try {
            FileInputStream fis = new FileInputStream(fname);
            ObjectInputStream ois = new ObjectInputStream(fis);
            bayes = (BayesClassifier<String, String>) ois.readObject(); 
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) { log(e); }
          catch (IOException e) { log(e); }
        log("Categories Total " + bayes.getCategoriesTotal());
        return bayes;
    }

    private static final Logger logger =
                         Logger.getLogger(ClassifierBayes.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }

    public static void main(String[] args) {

        /*
         * Create a new classifier instance. The context features are
         * Strings and the context will be classified with a String according
         * to the featureset of the context.
         */
        final Classifier<String, String> bayes =
                new BayesClassifier<String, String>();

        /*
         * The classifier can learn from classifications that are handed over
         * to the learn methods. Imagin a tokenized text as follows. The tokens
         * are the text's features. The category of the text will either be
         * positive or negative.
         */
        final String[] positiveText = "I love sunny days".split("\\s");
        bayes.learn("positive", Arrays.asList(positiveText));

        final String[] negativeText = "I hate rain".split("\\s");
        bayes.learn("negative", Arrays.asList(negativeText));

        /*
         * Now that the classifier has "learned" two classifications, it will
         * be able to classify similar sentences. The classify method returns
         * a Classification Object, that contains the given featureset,
         * classification probability and resulting category.
         */
        final String[] unknownText1 = "today is a sunny day".split("\\s");
        final String[] unknownText2 = "there will be rain".split("\\s");

        System.out.println( // will output "positive"
                bayes.classify(Arrays.asList(unknownText1)).getCategory());
        System.out.println( // will output "negative"
                bayes.classify(Arrays.asList(unknownText2)).getCategory());

        /*
         * The BayesClassifier extends the abstract Classifier and provides
         * detailed classification results that can be retrieved by calling
         * the classifyDetailed Method.
         *
         * The classification with the highest probability is the resulting
         * classification. The returned List will look like this.
         * [
         *   Classification [
         *     category=negative,
         *     probability=0.0078125,
         *     featureset=[today, is, a, sunny, day]
         *   ],
         *   Classification [
         *     category=positive,
         *     probability=0.0234375,
         *     featureset=[today, is, a, sunny, day]
         *   ]
         * ]
         */
        ((BayesClassifier<String, String>) bayes).classifyDetailed(
                Arrays.asList(unknownText1));

        /*
         * Please note, that this particular classifier implementation will
         * "forget" learned classifications after a few learning sessions. The
         * number of learning sessions it will record can be set as follows:
         */
        bayes.setMemoryCapacity(500); // remember the last 500 learned classifications
    }

}
