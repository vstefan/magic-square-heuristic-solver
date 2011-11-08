import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


class GAMagicSquare implements Comparable<GAMagicSquare>
{
    public int[][] square;
    public int     fitness;
    public double  normalisedFitness;
    public double  accumulatedNormalisedFitness;
    
    public GAMagicSquare(int[][] sq, int f)
    {
        square                       = sq;
        fitness                      = f;
        normalisedFitness            = 0.0;
        accumulatedNormalisedFitness = 0.0;
    }
    
    public int compareTo(GAMagicSquare rhs)
    {
        int result = 0;
        
        if(normalisedFitness < rhs.normalisedFitness)
        {
            result = -1;
        }
        else if(normalisedFitness > rhs.normalisedFitness)
        {
            result = 1;
        }
        
        return result;
    }
}


class GAMagicSquarePopulation
{
    public ArrayList<GAMagicSquare> population;
    public int                    fitnessSum;
    public int                    bestFitness;
    public int                    bestFitnessIndex;
    
    public GAMagicSquarePopulation(ArrayList<GAMagicSquare> p, int fSum)
    {
        population       = p;
        fitnessSum       = fSum;
        bestFitness      = 0;
        bestFitnessIndex = 0;
    }
    
    public GAMagicSquarePopulation(ArrayList<GAMagicSquare> p, int fSum, int bf, int bfI)
    {
        population       = p;
        fitnessSum       = fSum;
        bestFitness      = bf;
        bestFitnessIndex = bfI;
    }
}

public class GeneticAlgorithmSolver implements MagicSquareSolver
{
    // algorithm parameters
    private final int    POPULATION_SIZE  = 300;
    private final int    MATING_POOL_SIZE = POPULATION_SIZE/2;
    private final double MUTATION_PROB    = 0.10; // % chance of mutation
    private final int    MUTATION_COUNT   = 1;    // number of mutations
    
    private Random       rand;
    private ProgOptions  options;
    
    public GeneticAlgorithmSolver(ProgOptions progOptions)
    {
        rand    = new Random();
        options = progOptions;
    }
    
    public MagicSquareSolution solve(long allowedTimeNanoSec)
    {
        GAMagicSquarePopulation squares = createInitialPopulation(POPULATION_SIZE);
        
        //computeAccumulatedNormalisedFitness(squares.population);
        
        int generationCount = 0;
                
        long startTime = System.nanoTime();
                
        do
        {
            computeAndSortByNormalisedFitness(squares.population, squares.fitnessSum);
            
            ArrayList<GAMagicSquare> matingPool = createMatingPool(squares.population, MATING_POOL_SIZE);
            
            squares = createNextGeneration(matingPool, POPULATION_SIZE, options.n, options.m);
            
            System.out.println("Generation #" + generationCount + ": best fitness: " + squares.bestFitness);
            
            if(squares.bestFitness == 0)
            {
                break;
            }
            
            generationCount++;
            
        }while(System.nanoTime() - startTime < allowedTimeNanoSec);
        
        GAMagicSquare bestSolution = squares.population.get(squares.bestFitnessIndex);
         
        return new MagicSquareSolution(bestSolution.square, bestSolution.fitness, generationCount);
    }
    
    
    private GAMagicSquarePopulation createNextGeneration(
            ArrayList<GAMagicSquare> matingPool, 
            int                      size,
            int                      n,
            int                      m)
    {
        ArrayList<GAMagicSquare> nextGen = new ArrayList<GAMagicSquare>(size);
        int fitnessSum       = 0;
        int bestFitness      = Integer.MAX_VALUE;
        int bestFitnessIndex = 0;
        
        for(int i=0; i<size; i++)
        {   
            GAMagicSquare newSquare = crossoverAndMutate(matingPool.get(rand.nextInt(matingPool.size())), 
                                                         matingPool.get(rand.nextInt(matingPool.size())),
                                                         n, m);
            
            fitnessSum += newSquare.fitness;
            
            if(newSquare.fitness < bestFitness)
            {
                bestFitness      = newSquare.fitness;
                bestFitnessIndex = i;
            }
            
            nextGen.add(i, newSquare); 
        }
        
        return new GAMagicSquarePopulation(nextGen, fitnessSum, bestFitness, bestFitnessIndex);
    }
    
     
    private GAMagicSquare crossoverAndMutate(GAMagicSquare p1, GAMagicSquare p2, int n, int m)
    {
        int[][] childSquare = new int[n][n];
        
        int colCutIndex = rand.nextInt(n);
        
        for(int i=0; i<n; i++)
        {
            // take from p1: all values to left of column cut
            for(int j=0; j<colCutIndex; j++)
            {
                childSquare[i][j] = p1.square[i][j];
            }
            
            // take the rest from p2
            for(int j=colCutIndex; j<n; j++)
            {
                childSquare[i][j] = p2.square[i][j];
            }
        }
        
        /*
        System.out.println(colCutIndex);
        HelperFunctions.outputSquare(p1.square, n);
        System.out.println();
        HelperFunctions.outputSquare(p2.square, n);
        System.out.println();
        HelperFunctions.outputSquare(childSquare, n);
        System.out.println();
        */
        if(rand.nextDouble() < MUTATION_PROB)
        {
            mutate(childSquare, n);
        }
        
        HelperFunctions.repair(rand, childSquare, n);
        //HelperFunctions.outputSquare(childSquare, n);
       
        return new GAMagicSquare(childSquare, HelperFunctions.evalFitness(childSquare, n, m));
    }
    
    
    private void mutate(int[][] square, int n)
    {
        HelperFunctions.mutateSwapValuesInPlace(rand, square, n, MUTATION_COUNT);
    }
    
        
    private ArrayList<GAMagicSquare> createMatingPool(ArrayList<GAMagicSquare> population, int size)
    {
//        return createMatingPoolFromTournament(population, size);
        return createMatingPoolFromFittest(population, size);
    }
    
    
    private ArrayList<GAMagicSquare> createMatingPoolFromTournament(ArrayList<GAMagicSquare> population, int size)
    {
        ArrayList<GAMagicSquare> matingPool = new ArrayList<GAMagicSquare>(size);
        int matingPoolIndex               = 0;
        
        while(matingPoolIndex < size)
        {
            GAMagicSquare s1 = population.get(rand.nextInt(population.size()));
            GAMagicSquare s2 = population.get(rand.nextInt(population.size()));
            
            if(s1.fitness < s2.fitness)
            {
                matingPool.add(matingPoolIndex++, s1);                
            }
            else
            {
                matingPool.add(matingPoolIndex++, s2);
            }
        }
                
        return matingPool;
    }
    
    
    private ArrayList<GAMagicSquare> createMatingPoolFromFittest(ArrayList<GAMagicSquare> population, int size)
    {
        ArrayList<GAMagicSquare> matingPool = new ArrayList<GAMagicSquare>(size);
        int matingPoolIndex                 = 0;
        
        for(int i=0; matingPoolIndex<size; i++)
        {
            matingPool.add(matingPoolIndex++, population.get(i));
        }
                
        return matingPool;
    }
    
    
    private GAMagicSquarePopulation createInitialPopulation(int size)
    {
        // create initial population
        ArrayList<GAMagicSquare> population = new ArrayList<GAMagicSquare>(size);
        
        int fitnessSum = 0;
        
        for(int i=0; i<size; i++)
        {
            int[][] square = HelperFunctions.makeInitialSquare(rand, options.n, options.constrained, options.cRow, options.cCol); 
            int fitness    = HelperFunctions.evalFitness(square, options.n, options.m);
            fitnessSum    += fitness;
            
            population.add(i, new GAMagicSquare(square, fitness));
        }
        
        return new GAMagicSquarePopulation(population, fitnessSum);
    }
    
    
    private void computeAndSortByNormalisedFitness(ArrayList<GAMagicSquare> list, double fitnessSum)
    {
        // compute normalised fitness
        for(int i=0; i<list.size(); i++)
        {
            GAMagicSquare square = list.get(i);
            square.normalisedFitness = square.fitness / (double)fitnessSum;
        }
        
        // sort by normalised fitness
        Collections.sort(list);
    }
    
    
    private void computeAccumulatedNormalisedFitness(ArrayList<GAMagicSquare> list)
    {
        GAMagicSquare firstSquare = list.get(0);
        firstSquare.accumulatedNormalisedFitness = firstSquare.normalisedFitness;
        
        for(int i=1; i<list.size(); i++)
        {
            GAMagicSquare square = list.get(i);
            square.accumulatedNormalisedFitness = square.normalisedFitness + list.get(i-1).normalisedFitness;
        }
    }
    
    

}
