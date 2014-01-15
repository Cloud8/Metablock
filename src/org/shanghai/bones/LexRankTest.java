package org.shanghai.bones;

import java.util.ArrayList;
import java.util.List;


/**
 * A quick little test of LexRank. I manually constructed a similarity matrix
 * from the graphs in the LexRank paper, and just used dummy items to test out
 * how well it works. We end up with slightly different numbers than given in
 * the paper, but the ranking is the same or almost the same, so I think it
 * works well enough.
 */
public class LexRankTest {
	public static void main(String[] args) {
		double[][] similarity = { 
				{ 1.00, 0.35, 0.00, 0.15, 0.00, 0.25, 0.00, 0.25, 0.00, 0.00, 0.00 },
				{ 0.35, 1.00, 0.15, 0.25, 0.00, 0.15, 0.00, 0.25, 0.00, 0.15, 0.00 },
				{ 0.00, 0.15, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 },
				{ 0.15, 0.25, 0.00, 1.00, 0.00, 0.15, 0.25, 0.15, 0.00, 0.00, 0.00 },
				{ 0.00, 0.00, 0.00, 0.00, 1.00, 0.25, 0.00, 0.15, 0.15, 0.00, 0.15 },
				{ 0.25, 0.15, 0.00, 0.15, 0.25, 1.00, 0.00, 0.25, 0.00, 0.25, 0.00 },
				{ 0.00, 0.00, 0.00, 0.25, 0.00, 0.00, 1.00, 0.00, 0.00, 0.00, 0.00 },
				{ 0.25, 0.25, 0.00, 0.15, 0.15, 0.25, 0.00, 1.00, 0.25, 0.25, 0.15 },
				{ 0.00, 0.00, 0.00, 0.00, 0.15, 0.00, 0.00, 0.25, 1.00, 0.25, 0.35 },
				{ 0.00, 0.15, 0.00, 0.00, 0.00, 0.25, 0.00, 0.25, 0.25, 1.00, 0.15 },
				{ 0.00, 0.00, 0.00, 0.00, 0.15, 0.00, 0.00, 0.15, 0.35, 0.15, 1.00 }, };
		
		List<DummyItem> items = new ArrayList<DummyItem>();
		for (int i = 0; i < similarity.length; ++i) {
			items.add(new DummyItem(i, similarity));
		}
		
		LexRankResults<DummyItem> results = LexRanker.rank(items, 0.1, false);
		String[] names = { "d1s1", "d2s1", "d2s2", "d2s3", "d3s1", "d3s2", "d3s3", "d4s1", "d5s1", "d5s2", "d5s3" };
		
		double max = results.scores.get(results.rankedResults.get(0));
		for (int i = 0; i < similarity.length; ++i) {
			System.out.println(names[i] + ": " + (results.scores.get(items.get(i)) / max));
		}
	}
}
