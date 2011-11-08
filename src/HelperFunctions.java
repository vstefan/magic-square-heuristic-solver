import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class HelperFunctions
{
    
    public static int[][] makeInitialSquare(
        Random  rand,
        int     n,
        boolean constrained,
        int     cRow,
        int     cCol)
    {
        int[][] square = new int[n][n];
        
        int nSquared     = n*n;
        int numPoolStart = 1;
        int numPoolSize  = nSquared;
        
        // constrained problem removes 1->9 as valid numbers 
        // for initial square population
        if(constrained)
        {
            numPoolStart = 10;
            numPoolSize  -= 9;
            
            // populate submatrix
            square[cRow][cCol]     = 1;
            square[cRow][cCol+1]   = 2;
            square[cRow][cCol+2]   = 3;
            square[cRow+1][cCol]   = 4;
            square[cRow+1][cCol+1] = 5;
            square[cRow+1][cCol+2] = 6;
            square[cRow+2][cCol]   = 7;
            square[cRow+2][cCol+1] = 8;
            square[cRow+2][cCol+2] = 9;
            
        }
        
        int numPoolIndex = 0;
        ArrayList<Integer> numPool = new ArrayList<Integer>(numPoolSize);
        
        for(int i=numPoolStart; i<=nSquared; i++)
        {
            numPool.add(numPoolIndex++, i);
        }
        
        for(int i=0; i<n; i++)
        {
            for(int j=0; j<n; j++)
            {
                if(0 == square[i][j])
                {
                    int indx     = rand.nextInt(numPool.size());
                    square[i][j] = numPool.get(indx);
                    
                    numPool.remove(indx);
                }
            }
        }
        
        return square;
    }
    
    
    public static int evalFitness(int[][] square, int n, int m)
    {
        int fitness            = 0;
        
        int totalRowDiff       = 0;
        int totalColDiff       = 0;
        int leftToRightDiagSum = 0;
        int rightToLeftDiagSum = 0;
        
        for(int i=0; i<n; i++)
        {
            int rowSum = 0;
            int colSum = 0;
            
            for(int j=0; j<n; j++)
            {
                rowSum += square[i][j];
                colSum += square[j][i];
                
                if(i == j)
                {
                    leftToRightDiagSum += square[i][j];
                }
                
                if(i+j == n-1)
                {
                    rightToLeftDiagSum += square[i][j];
                }
            }
            
            totalRowDiff += Math.abs(rowSum-m);
            totalColDiff += Math.abs(colSum-m);
        }
                
        //System.out.println(totalRowDiff);
        //System.out.println(totalColDiff);
        
        // fitness = sum of all deviations
        // closer to 0 the better
        
        // fitness is based on degree of deviation in the sums of the rows,
        // columns & diagonals.
        fitness = Math.abs(leftToRightDiagSum-m) + Math.abs(rightToLeftDiagSum-m) + totalRowDiff + totalColDiff;
                
        return fitness;
    }

    
    public static void outputSquare(int[][] square, int n)
    {
        for(int i=0; i<n; i++)
        {
            for(int j=0; j<n; j++)
            {
                System.out.print(square[i][j] + " ");
            }
            
            System.out.println();
        }
    }    

    
    // crossover / mutation can result in an illegal square
    // need to figure out which numbers are repeated and swap them
    // for numbers that are missing
    public static void repair(Random rand, int[][] square, int n)
    {
        HashSet<Integer> found = new HashSet<Integer>();
        
        LinkedList<Duplicate> dups = new LinkedList<Duplicate>();
        
        for(int i=0; i<n; i++)
        {
            for(int j=0; j<n; j++)
            {
                if(!found.add(square[i][j]))
                {
                    dups.add(new Duplicate(square[i][j], i, j));
                }
            }
        }
        
        ArrayList<Integer> missing = new ArrayList<Integer>();
        
        for(int i=1; i<=n*n; i++)
        {
            if(!found.contains(i))
            {
                missing.add(i);
            }
        }
        
        for(Duplicate dup : dups)
        {
            int missingIndex         = rand.nextInt(missing.size());
            square[dup.row][dup.col] = missing.get(missingIndex);
            
            missing.remove(missingIndex);
        }
    }
}
