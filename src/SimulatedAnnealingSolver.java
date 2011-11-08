import java.util.Random;

public class SimulatedAnnealingSolver implements MagicSquareSolver
{
    // algorithm parameters
    private double INTIAL_TEMPERATURE  = 1.0;
    private int    ITERATIONS          = 500;   // 500
    private int    STEPS_PER_ITERATION = 1000;  // 1000
    private double COOLING_FRACTION    = 0.97;
    private double BOLTZMANNS_CONSTANT = 0.01;
    
    private Random      rand;
    private ProgOptions options;
    
    public SimulatedAnnealingSolver(ProgOptions progOptions)
    {
        rand    = new Random();
        options = progOptions;
    }
    
    public MagicSquareSolution solve()
    {
        int[][] square = HelperFunctions.makeInitialSquare(rand, options.n, options.constrained, options.cRow, options.cCol);
        int fitness    = HelperFunctions.evalFitness(square, options.n, options.m);
        int iteration  = 0;
        
        double temperature = INTIAL_TEMPERATURE;
        
        for(int i=0; i<ITERATIONS; i++)
        {
            if(fitness == 0)
            {
                break;
            }
            
            temperature *= COOLING_FRACTION;
            int tempFitness = fitness;
            
            for(int j=0; j<STEPS_PER_ITERATION; j++)
            {
                int[][] mutatedSquare = mutate(square, options.n);
                int newFitness        = HelperFunctions.evalFitness(mutatedSquare, options.n, options.m);
                
                if(newFitness < fitness || 
                   Math.exp((fitness-newFitness)/(BOLTZMANNS_CONSTANT*temperature)) > rand.nextDouble())
                {
                    square  = mutatedSquare;
                    fitness = newFitness;
                }
                
                if(fitness == 0)
                {
                    break;
                }
            }
            
            if(tempFitness != fitness)
            {
                temperature /= COOLING_FRACTION;
            }
            
            System.out.println("Iteration #" + iteration + ": best fitness: " + fitness);
            
            iteration++;
        }
        
        return new MagicSquareSolution(square, fitness, iteration); 
    }
    
    private int[][] mutate(int[][] square, int n)
    {
        int[][] copy = square.clone();
        
        int r1 = rand.nextInt(options.n);
        int c1 = rand.nextInt(options.n);
        int r2 = rand.nextInt(options.n);
        int c2 = rand.nextInt(options.n);
        
        int temp     = copy[r1][c1];
        copy[r1][c1] = copy[r2][c2];
        copy[r2][c2] = temp;
        
        return copy;
    }

}
