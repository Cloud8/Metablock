package org.shanghai.bones;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.StringBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop 
  @title Program to Summarize a Text
  @date 2013-12-12
*/
public class LexSummary {

    private List<String> sentenceTexts;
	private List<String> results;

	public LexSummary() {
    }

	public LexSummary(String text) {
        String[] strs = text.split("(?<=[.?!])\\s+(?=[a-zA-Z])"); 
		this.sentenceTexts = new ArrayList<String>(Arrays.asList(strs));
	}

	public LexSummary(List<String> sentences) {
		this.sentenceTexts = sentences;
	}

    public void dispose() {
    }

    public void create() {
    }

    public String title(String text) {
        //String str;
        //if (results==null)
        //    str = summary(1, text);
        //else str = results.get(0);
        //String[] words = tokens(str); // str.split("\\s+");
        //StringBuilder sb = new StringBuilder();
        //for (int i=0; i<10 && i<words.length; i++) {
        //    sb.append(words[i]);
        //    if (i<9) sb.append(" ");
        //}
        //sb.append("...");
        //return sb.toString();
        int x = text.indexOf(' ',64);
        x=x>0?x:text.length();
        return text.substring(0,x);
    }

    public String summary(String text) {
        String str = summary(6, text);
        str = str.replaceAll("\\s+", " ");
        return str;
    }

    public String summary(int level, String text) {
        String[] strs = text.split("(?<=[.?!])\\s+(?=[a-zA-Z])"); 
		this.sentenceTexts = new ArrayList<String>(Arrays.asList(strs));
		results = summarize();
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<level && i<results.size(); i++) {
            sb.append(results.get(i));
            if (i<level-1) sb.append(" ");
        }
        return sb.toString();
    }

    private void log(String msg) {
        System.out.println("Summariser " + ": " + msg);
    }

    private void log(Exception e) {
        log(e.toString());
    }

	/**
	 * Tokenize a string's text, so that this and Sentence come
	 * up with the same result. 
	 */
	public static String[] tokens(String sentenceText) {
		return sentenceText.replaceAll("[^a-zA-Z0-9_'\\s]", "").toLowerCase()
				.split("\\s+");
	}

	/**
	 * Computes the inverse document frequency for each word in the corpus. 
     * IDF is defined as log(N/t), where N is the number of documents
	 * and t is the number of documents that term appears in.
	 */
	private Map<String, Double> idf(List<String> words) {
		Map<String, Double> idf = new HashMap<String, Double>();
		Map<String, Integer> df = new HashMap<String, Integer>();
		for (String word : words) {
			df.put(word, 0);
		}
		for (String sentence : sentenceTexts) {
			Set<String> present = new HashSet<String>();
			for (String word : tokens(sentence)) {
				present.add(word);
			}
			for (String word : present) {
				df.put(word, df.get(word) + 1);
			}
		}
		for (String word : words) {
			idf.put(word, Math.log(sentenceTexts.size() * 1.0 / df.get(word)));
		}
		return idf;
	}

	/**
	 * Generates a summary of the sentences passed into this DocumentSummarizer. The
	 * output is a list of salient sentences, ordered from most salient to least.
	 * Currently, we just take any sentence that is locally maximal in relevance,
	 * so it's possible that the summary could have multiple entries that mean the
	 * same thing. Anecdotally, though, it work pretty well.
	 */
	public List<String> summarize() {
		Set<String> wordSet = new HashSet<String>();
		for (String s : sentenceTexts) {
			for (String word : tokens(s)) {
				wordSet.add(word);
			}
		}
		List<String> words = new ArrayList<String>();
		for (String s : wordSet) {
			words.add(s);
		}
		Map<String, Double> idf = idf(words);
		List<Sentence> sentences = new ArrayList<Sentence>(sentenceTexts.size());
		for (String s : sentenceTexts) {
			sentences.add(new Sentence(s, idf, words));
		}
	    //LexRankResults<Sentence> results = LexRanker.rank(sentences, 0.1, false);
	    LexRankResults<Sentence> results 
                                       = LexRanker.rank(sentences, 0.1, false);

		List<String> finalResults = new ArrayList<String>();
		for (Sentence c : results.rankedResults) {
			// Only return results that are local maxima
			boolean max = true;
			//for (Sentence neighbor : results.neighbors.get(c)) {
            if (results.neighbors.get(c)==null) {
                //System.out.println("zero neighbor");
				max = true;
            } else {
			for (Sentence neighbor : results.neighbors.get(c)) {
				if (results.scores.get(neighbor) > results.scores.get(c)) {
					max = false;
				}
			}
            }
			if (max) {
                String text = c.sentenceText.trim();
                if (text.length()>0)
				finalResults.add(text);
			}
		}
		return finalResults;
	}

}
