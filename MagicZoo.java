import java.util.*;

public class MagicZoo {
	public static void main(final String [] param) {
		if (param.length!=3) {
			printUsage();
		} else {
			try {
				final long numGoats = Long.valueOf(param[0]);
				final long numWolves = Long.valueOf(param[1]);
				final long numLions = Long.valueOf(param[2]);
				if (numGoats < 0 || numWolves < 0 || numLions < 0) {
					System.out.println("There is insufficient magic in the zoo for negative animals.");
				} else {
					printMaximumStableSolution(new long[]{numGoats, numWolves, numLions});
				}
			} catch (final NumberFormatException e) {
				printUsage();
			}
		}
	}
	
	private static void printUsage() {
		System.err.println("Syntax: MagicZoo numGoats numWolves numLions");
		System.err.println("Example: MagicZoo 10 20 30");
	}
	
	private static void printMaximumStableSolution(final long populations[]) {
		// Print the reasoning for choosing A (remaining animals), B (biggest pop of the non-remaining), C (smallest pop of the non-remaining).
		printReasoning();
		
		// Find out whether B and C are going to be odd or even. We need whichever occurs the most.
		int numOdd = 0;
		for (byte i = 0; i < 3; i++) {
			numOdd += populations[i] & 1;	// Add the least significant bit, which signifies odd (1) or even (0). This conveniently counts the odd numbers.
		}
		final boolean majorityIsOdd = numOdd > 1;
		
		int indexOfSmallestPopInMajority = -1;	// C.
		int indexOfSecondSmallestPopInMajority = -1;	// B.
		for (byte i = 0; i < 3; i++) {
			final long currentPop = populations[i];
			// Test if the current population is from the the majority. (Odd or even.)
			if (((currentPop & 1)==1)==majorityIsOdd) {
				if (indexOfSmallestPopInMajority==-1 || currentPop < populations[indexOfSmallestPopInMajority]) {
					// Smallest so far.
					indexOfSecondSmallestPopInMajority = indexOfSmallestPopInMajority;	// Previous smallest now becomes second smallest.
					indexOfSmallestPopInMajority = i;
				} else if (indexOfSecondSmallestPopInMajority==-1 || currentPop < populations[indexOfSecondSmallestPopInMajority]) {
					// Second smallest so far.
					indexOfSecondSmallestPopInMajority = i;
				}
			}
		}
		// The indices will always add up to 0+1+2=3. The remaining index is easily found by subtracting the other two from 3.
		int indexForRemainingAnimals = 3 - indexOfSmallestPopInMajority - indexOfSecondSmallestPopInMajority;	// A.
		
		// Could be that there is more than one possible solution.
		final byte numOptionsForRemaining;
		if (populations[indexForRemainingAnimals]==populations[indexOfSecondSmallestPopInMajority]) {
			if (populations[indexOfSmallestPopInMajority]==populations[indexOfSecondSmallestPopInMajority]) {
				// The 3 populations are equally big, so we have 3 options for the remaining population.
				numOptionsForRemaining = 3;
			} else {
				// We could have switched A and B, since they are equally big.
				numOptionsForRemaining = 2;
			}
		} else {
			// We got the only right choice for A. (It can't be equal to the smallest in the majority and unequal to the second smallest, because then it
			// should have been chosen as the second smallest.)
			numOptionsForRemaining = 1;
		}
		
		populations[indexForRemainingAnimals] += populations[indexOfSmallestPopInMajority];	// #A + #C
		populations[indexOfSmallestPopInMajority] = 0;
		populations[indexOfSecondSmallestPopInMajority] = 0;
		
		final StringBuilder sb = new StringBuilder("It follows that the biggest possible stable population from the specified starting populations has ");
		appendPops(sb, populations[0], populations[1], populations[2]);
		// Print the alternate solutions, if any.
		switch (numOptionsForRemaining) {
			case 3:
				sb.append("; or ");
				swap(populations, indexForRemainingAnimals, indexOfSmallestPopInMajority);
				appendPops(sb, populations[0], populations[1], populations[2]);
				swap(populations, indexOfSmallestPopInMajority, indexForRemainingAnimals);
				// Intentional fall-through. We really don't want a break here.
			case 2:
				sb.append("; or ");
				swap(populations, indexForRemainingAnimals, indexOfSecondSmallestPopInMajority);
				appendPops(sb, populations[0], populations[1], populations[2]);
				// Intentional fall-through. We really don't want a break here.
			case 1:
				sb.append('.');
				// Intentional fall-through. A break doesn't do anything here.
		}
		System.out.println(sb);
	}
	
	private static void printReasoning() {
		System.out.println("There can be no stable population if there are 2 different kinds of animals, since any 2 different animals can produce a third kind of animal.\n");
		
		System.out.println("A population of 1 or 0 kinds of animals is always stable, since there is no applicable rule.\n");
		
		System.out.println("Each time we apply a rule, 2 animals are removed and 1 is added, so a net reduction of 1. It follows that the the biggest stable population");
		System.out.println("is reached by applying the rules as few times as possible until at most 1 type of animal is left.\n");
		
		System.out.println("Let's call the type of animal that remains in the final situation A. The other ones are B and C. The initial populations are #A, #B, #C.\n");
		
		System.out.println("There is no single rule to remove more than one animal of the same kind. So we will need at least max(#B, #C) rule applications to remove all B and C.\n");
		
		System.out.println("We want to use B+C=A as much as possible, since those get rid of B and C while producing more A. We can initially do this min(#B, #C) times.\n");
		
		System.out.println("If #B != #C, then we'll have some B or C left. Choose B so that #B > #C, so we're stuck with some extra B.\n");
		
		System.out.println("To get rid of these, we need to sacrifice some A to produce C. We can then once more use B+C=A.\n");
		
		System.out.println("Each application of A+B=C and B+C=A results in a new reduction of 2 B. So if the number of B starts off odd, it will remain odd. (Same for even.)\n");
		
		System.out.println("To get rid of all B and C, we need to end up in a situation where there are an equal number of B and C, so both numbers need to be odd, or both even.");
		System.out.println("So we choose B and C such that #B and #C are both odd or both even, and as small as possible in case all 3 numbers are of the same type (odd or even).\n");
		
		System.out.println("We apply A+B=C followed by B+C=A until there are no more B and C left. We need to do this (#B-#C)/2 times. The net effect is that we remove 2 B by");
		System.out.println("\"temporarily\" using an A. This requires 2 * (#B-#C)/2) = #B - #C rule applications.\n");
		
		System.out.println("There are no animals of types B or C left. The number of animals of type A is #A + min(#B, #C) = #A + #C. We used #C + (#B - #C) = #B rule applications.\n");
		
		System.out.println("We couldn't have used fewer rule applications to get rid of all the B. (We used #B = max(#B, #C), which we proved to be the minimum.) There are only");
		System.out.println("2 B-reducing rules. Of these, we couldn't have used the A-producing rule more often, or used the A-reducing rule less often. Therefore the stable");
		System.out.println("population is as big as it can possibly be.\n");
	}
	
	private static void appendPops(final StringBuilder sb, final long numGoats, final long numWolves, final long numLions) {
		sb.append(numGoats).append(" goats, ").append(numWolves).append(" wolves, and ").append(numLions).append(" lions");
	}
	
	private static void swap(final long populations[], final int a, final int b) {
		final long l = populations[a];
		populations[a] = populations[b];
		populations[b] = l;
	}
}
