package org.seaview.cite;

import org.shanghai.util.FileUtil;
import org.shanghai.util.TextUtil;
import org.seaview.cite.RefSentiment;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.lang.StringBuilder;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;

/**
   @title Stanford Sentiment analysis
   @date 2015-12-16
 */
public class ClassifierStanford implements RefSentiment.Classifier {

    private String modelFile;

    private String text;
    private int count;

    private StanfordCoreNLP tokenizer;
    private StanfordCoreNLP pipeline;

    public ClassifierStanford(String loadModel) {
        modelFile = loadModel;
    }

    @Override
    public void dispose() {
        log("disposed.");
    }

    @Override
    public ClassifierStanford create() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
        //props.put("annotators", "tokenize, ssplit, pos, parse, sentiment");
        //props.setProperty("tokenize.options","normalizeCurrency=false");
        // Need a good model. Defaults to treebank.
        //props.put("sentiment.model", "/path/to/model-0014-93.73.ser.gz.");
        pipeline = new StanfordCoreNLP(props);
        return this;
    }

    @Deprecated
    private ClassifierStanford createOne() {
        Properties tokenizerProps = new Properties();
		tokenizerProps.setProperty("annotators", "tokenize, ssplit");
        tokenizer = new StanfordCoreNLP(tokenizerProps);

        Properties pipelineProps = new Properties();
        //pipelineProps.setProperty("sentiment.model", sentimentModel);
		//pipelineProps.setProperty("parse.model", parserModel);
		pipelineProps.setProperty("annotators", "parse, sentiment");
		pipelineProps.setProperty("enforceRequirements", "false");
        pipeline = new StanfordCoreNLP(pipelineProps);
        return this;
    }

    @Override
    public void probe() {
        log("probe " + this.getClass().getSimpleName());
    }

    @Deprecated
    public String classifyOne(String line) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Annotation annotation = pipeline.process(line);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            if (count>0) {
                sb.append(" ");
            }
            // outputTree(System.out, sentence, outputFormats);
            //result += sentence.get(SentimentCoreAnnotations.ClassName.class);
            Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
            //tree.pennPrint(System.out);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            sb.append(sentiment); 
            count++;
        }
        return sb.toString();
    }

    @Override
    public void learn(String opinion, String text) {
        // we can't
    }

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }

    @Override
    public String classify(String line) {
        return classify(line, false);
    }

    public String classify(String line, boolean test) {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        Annotation annotation;
        if (tokenizer==null) {
            annotation = new Annotation(line);
        } else {
            annotation = tokenizer.process(line);
        }
        pipeline.annotate(annotation);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            if (count>0) {
                sb.append(" ");
            }
            //outputTree(System.out, sentence, outputFormats);
            sb.append(sentence.get(SentimentCoreAnnotations.ClassName.class));
            count++;
        }
        String stanford = sb.toString();
        if (test) {
            //log("#########################################");
            log(line);
            pipeline.prettyPrint(annotation, System.out);
            log("## " + stanford);
            log("#########################################");
        }
        if (stanford.contains("Negative")) { // very Negative
            return "n";
        } else if (stanford.contains("negative")) {
            return "n";
        } else if (stanford.contains("Neutral")) {
            return "o";
        } else if (stanford.contains("Positive")) {
            return "p";
        } else if (stanford.contains("positive")) {
            return "p";
        } else {
            return stanford;
        }
    }

    private void sentimentTree(String line) {
        Annotation annotation = tokenizer.process(line);
        pipeline.annotate(annotation);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            outputTree(System.out, sentence, outputFormats);
        }
    }

  /**
   * Outputs a tree using the output style requested
   */
  static void outputTree(PrintStream out, CoreMap sentence, List<Output> outputFormats) {
    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
    for (Output output : outputFormats) {
      switch (output) {
      case PENNTREES: {
        Tree copy = tree.deepCopy();
        setSentimentLabels(copy);
        out.println(copy);
        break;
      }
      case VECTORS: {
        Tree copy = tree.deepCopy();
        setIndexLabels(copy, 0);
        out.println(copy);
        outputTreeVectors(out, tree, 0);
        break;
      }
      case ROOT: {
        out.println("  " + sentence.get(SentimentCoreAnnotations.ClassName.class));
        break;
      }
      case PROBABILITIES: {
        Tree copy = tree.deepCopy();
        setIndexLabels(copy, 0);
        out.println(copy);
        outputTreeScores(out, tree, 0);
        break;
      }
      default:
        throw new IllegalArgumentException("Unknown output format " + output);
      }
    }
  }

  static enum Output {
    PENNTREES, VECTORS, ROOT, PROBABILITIES
  }

  List<Output> outputFormats = Collections.singletonList(Output.ROOT);

  /**
   * Sets the labels on the tree (except the leaves) to be the integer
   * value of the sentiment prediction.  Makes it easy to print out
   * with Tree.toString()
   */
  static void setSentimentLabels(Tree tree) {
    if (tree.isLeaf()) {
      return;
    }

    for (Tree child : tree.children()) {
      setSentimentLabels(child);
    }

    Label label = tree.label();
    if (!(label instanceof CoreLabel)) {
      throw new IllegalArgumentException("Required a tree with CoreLabels");
    }
    CoreLabel cl = (CoreLabel) label;
    cl.setValue(Integer.toString(RNNCoreAnnotations.getPredictedClass(tree)));
  }

  /**
   * Sets the labels on the tree to be the indices of the nodes.
   * Starts counting at the root and does a postorder traversal.
   */
  static int setIndexLabels(Tree tree, int index) {
    if (tree.isLeaf()) {
      return index;
    }

    tree.label().setValue(Integer.toString(index));
    index++;
    for (Tree child : tree.children()) {
      index = setIndexLabels(child, index);
    }
    return index;
  }

  /**
   * Outputs the vectors from the tree.  Counts the tree nodes the
   * same as setIndexLabels.
   */
  static int outputTreeVectors(PrintStream out, Tree tree, int index) {
    if (tree.isLeaf()) {
      return index;
    }

    out.print("  " + index + ":");
    SimpleMatrix vector = RNNCoreAnnotations.getNodeVector(tree);
    for (int i = 0; i < vector.getNumElements(); ++i) {
      out.print("  " + NF.format(vector.get(i)));
    }
    out.println();
    index++;
    for (Tree child : tree.children()) {
      index = outputTreeVectors(out, child, index);
    }
    return index;
  }

  /**
   * Outputs the scores from the tree.  Counts the tree nodes the
   * same as setIndexLabels.
   */
  static int outputTreeScores(PrintStream out, Tree tree, int index) {
    if (tree.isLeaf()) {
      return index;
    }

    out.print("  " + index + ":");
    SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);
    for (int i = 0; i < vector.getNumElements(); ++i) {
      out.print("  " + NF.format(vector.get(i)));
    }
    out.println();
    index++;
    for (Tree child : tree.children()) {
      index = outputTreeScores(out, child, index);
    }
    return index;
  }

  private static final NumberFormat NF = new DecimalFormat("0.0000");

    private static final Logger logger =
                         Logger.getLogger(ClassifierStanford.class.getName());

    protected void log(String msg) {
        //logger.info(msg);
        System.out.println(msg);
    }

}
