public class MSMain
{
    private enum ParseState { NONE, N,  P_ROW, P_COL };
    
    private static ParseState parseState = ParseState.NONE;    
    
    public static void main(String[] args)
    {
        ProgOptions options = new ProgOptions();
        
        for(String arg : args)
        {
            if(ParseState.N == parseState)
            {
                options.n = Integer.parseInt(arg);
                options.m = (options.n*((options.n*options.n) + 1))/2;                
                
                parseState = ParseState.NONE;
            }
            else if(ParseState.P_ROW == parseState)
            {
                options.cRow = Integer.parseInt(arg);
                parseState = ParseState.P_COL;
            }
            else if(ParseState.P_COL == parseState)
            {
                options.cCol = Integer.parseInt(arg);
                parseState = ParseState.NONE;
            }
            else
            {
                if(arg.equals("-n"))
                {
                    parseState = ParseState.N;
                }
                else if(arg.equals("-c"))
                {
                    options.constrained = true;
                }
                else if(arg.equals("-p"))
                {
                    parseState = ParseState.P_ROW;
                }
            }
        }
        
//        MagicSquareSolver solver = new GeneticAlgorithmSolver(options);
        MagicSquareSolver solver = new SimulatedAnnealingSolver(options);
        
        MagicSquareSolution bestSolution = solver.solve();
        
        HelperFunctions.outputSquare(bestSolution.square, options.n);
        
        
        
    }
}
