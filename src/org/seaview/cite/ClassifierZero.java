package org.seaview.cite;

import java.util.logging.Logger;
import java.util.Random;

/**
   @title Dummy Classifier for Sentiment Analysis
   @date 2016-01-03
 */
public class ClassifierZero implements RefSentiment.Classifier {

    private String modelFile;
    private Random rand;

    public ClassifierZero(String modelFile) {
        this.modelFile = modelFile;
    }

    @Override
    public RefSentiment.Classifier create() {
        rand = new Random();
        return this;
    }

    @Override
    public void dispose() {
        log("disposed " + this.getClass().getSimpleName());
    }

    @Override
    public void probe() {
        log("probe " + this.getClass().getSimpleName());
    }

    @Override
    public void learn(String opinion, String text) {
        // dont learn
    }

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }

    @Override
    public String classify(String text) {
        switch(rand.nextInt(3)) {
            case 0: return "n"; // NEGATIVE
            case 1: return "o"; // NEUTRAL
            case 2: return "p"; // POSITIVE
            default: return "trash";
        }
    }

    private static final Logger logger =
                         Logger.getLogger(ClassifierZero.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

}
