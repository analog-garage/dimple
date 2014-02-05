package com.analog.lyric.dimple.benchmarks.imageDenoising;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import com.analog.lyric.dimple.benchmarks.utils.ArrayM;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Bit;
import com.google.common.primitives.Doubles;

public class ImageDenoisingGraph
{
	private Bit[][] _vs;
	private int _rows;
	private int _cols;

	ImageDenoisingGraph(final FactorGraph fg, final String factorFileName,
			final int xImageSize, final int yImageSize, final int xBlockSize,
			final int yBlockSize, final boolean verbose)
	{

		int blockSize = xBlockSize * yBlockSize;

		int[] dims = new int[blockSize];
		for (int i = 0; i < dims.length; i++)
		{
			dims[i] = 2;
		}
		double[] factorTableValues = loadFactorTableValues(factorFileName);

		_rows = yImageSize;
		_cols = xImageSize;
		int blockRows = _rows - yBlockSize + 1;
		int blockCols = _cols - xBlockSize + 1;

		if (verbose)
		{
			System.out.println("Creating variables");
		}
		_vs = new Bit[_rows][_cols];
		// for row = 1:rows
		for (int row = 0; row < _rows; row++)
		{
			// Vs(row,:).setNames(['V_row' num2str(row)]);
			for (int col = 0; col < _cols; col++)
			{
				_vs[row][col] = new Bit();
				_vs[row][col].setName(String.format("V_row%d_vv%d", row, col));
			}
		}

		if (verbose)
		{
			System.out.println("Done creating variables");
		}

		DiscreteDomain[] domains = new DiscreteDomain[blockSize];
		for (int i = 0; i < domains.length; i++)
		{
			domains[i] = DiscreteDomain.create(0, 1);
		}
		IFactorTable factorTable = FactorTable.create(domains);
		factorTable.setWeightsDense(factorTableValues);

		// yList = 1:blockRows;
		Bit[] varPatch = new Bit[blockSize];
		for (int yList = 0; yList < blockRows; yList++)
		{
			// xList = 1:blockCols;
			for (int xList = 0; xList < blockCols; xList++)
			{
				// tempVar = Bit(); % Do this to avoid creating a whole array of
				// temp
				// variables
				// varPatches =
				// repmat(tempVar,[blockCols,blockRows,xBlockSize*yBlockSize]);
				// blockOffset = 1;
				int blockOffset = 0;
				// for yb = 0:yBlockSize-1
				for (int yb = 0; yb < yBlockSize - 1; yb++)
				{
					// for xb = 0:xBlockSize-1
					for (int xb = 0; xb < xBlockSize - 1; xb++)
					{
						// TODO: Was this a bug? Reversing rows and columns...
						// varPatches(:,:,blockOffset) = Vs(xb+xList,yb+yList);
						varPatch[blockOffset] = _vs[yb + yList][xb + xList];
						 blockOffset = blockOffset + 1;
						// end
						// end
					}
				}
				// fg.addFactorVectorized(factorTable,{varPatches,[1,2]});
				fg.addFactor(factorTable,  varPatch);
			}
		}
	}

	private double[] loadFactorTableValues(String factorFileName)
	{
		InputStream i = this.getClass().getResourceAsStream(factorFileName);
		try
		{
			Scanner scanner = new Scanner(i);
			ArrayList<Double> m = new ArrayList<Double>();
			while (scanner.hasNextDouble())
			{
				m.add(scanner.nextDouble());
			}
			double[] data = Doubles.toArray(m);
			return data;
		}
		finally
		{
			if (i != null)
			{
				try
				{
					i.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	public void setInput(ArrayM likelihoods)
	{
		for (int[] coordinates : likelihoods)
		{
			int row = coordinates[0];
			int col = coordinates[1];
			_vs[row][col].setInput(likelihoods.get(coordinates));
		}
	}

	public double getValue(int row, int col)
	{
		return (Double)(_vs[row][col].getValue());
	}

}
