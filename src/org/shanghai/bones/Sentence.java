package org.shanghai.bones;

import java.util.List;
import java.util.Map;

public class Sentence implements Similar<Sentence> {
	String sentenceText;
	double[] tfIDF; // best variable name ever

	public Sentence(String text, Map<String, Double> idf, List<String> terms) {
		this.sentenceText = text;
		tfIDF = new double[terms.size()];
		String normalizedText = text.replaceAll("\\s+", " ")
				.replaceAll("[^a-zA-Z0-9_'\\s]", "").toLowerCase();
		for (int i = 0; i < terms.size(); ++i) {
			tfIDF[i] = 0;
		}
		for (String term : normalizedText.split("\\s+")) {
			for (int i = 0; i < terms.size(); ++i) {
				tfIDF[i] += wordSimilarity(term, terms.get(i)) * idf.get(term);
			}
		}
	}

	protected static double wordSimilarity(String word1, String word2) {
		// TODO: hook this up to WordNet, because, seriously.
		// ALSO TODO: then it would be nice to cache these values.
		if (word1.equals(word2)) {
			return 1;
		}
		return 0;
	}

	@Override
	public double similarity(Sentence other) {
		// TODO: maybe do some caching here too?
		double dotProduct = 0;
		double magnitude = 0;
		double magnitudeOther = 0;
		for (int i = 0; i < tfIDF.length; ++i) {
			dotProduct += tfIDF[i] * other.tfIDF[i];
			magnitude += tfIDF[i] * tfIDF[i];
			magnitudeOther += other.tfIDF[i] * other.tfIDF[i];
		}
		return dotProduct / Math.sqrt(magnitude * magnitudeOther);
	}
}
