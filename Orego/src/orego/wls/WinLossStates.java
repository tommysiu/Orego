package orego.wls;
/*	DESCRIPTION:
	============

	This file includes the implementation of an object to initialize the WIN[]
	LOSS[] arrays.

	WLS is described in:
	--------------------

	Win/Loss States: An efficient model of success rates for simulation-based
	functions	Jacques Basald�a and J. Marcos Moreno Vega

	Since the arrays are small, static allocation is used. You can call the
	method BuildTables() to create the arrays any number of times.


	Method: BuildTables(EndScale, IniWins, IniVisits	: integer)
	-------------------

		EndScale 	: End Of Scale. Note that 21 is the maximum that fits in
					  8 bit. To use larger values, you will have to increase
					  the array sizes above [256].

		IniWins,
		IniVisits	: Both together form a virtual a priori information that
					  applies to the state 0 (= never seen).
					  E.g., IniWins = IniVisits = 1 means: When an event has
					  never been observed, if the first time seen the event
					  is a success (win), it will go to the state (2/2). If the
					  first time is a failure (loss) it will go to (1/2).

		Usual value	: BuildTables(21, 0, 0) or BuildTables(21, 1, 1)


	Public variables:
	-----------------

		wls_State_Best		: The highest state as binary (E.g., 14/14)
		wls_EndScale		: The value of EndScale passed to the previous
							  BuildTables() call.
		wls_binThresh_1div2	: Binary value of 1/2. This plus or minus some
							  offset is the value used in a simulation as a
							  threshold.
		WIN					: LUT table to update the WLS after a win
								aWls = wt.WIN[aWls];
		LOSS				: LUT table to update the WLS after a loss
								aWls = wt.LOSS[aWls];
							  (where aWls is an integer storing the state and
							   wt is an instance of the class WLStables)


	Multilingual: This file exists in Pascal (Delphi), C++ and Java.
	-------------


	LICENSE:
	========

		Copyright (c) 2011 Jacques Basald�a.
		All rights reserved.

	Redistribution and use in source and binary forms are permitted
	provided that the above copyright notice and this paragraph are
	duplicated in all such forms and that any documentation,
	advertising materials, and other materials related to such
	distribution and use acknowledge that the software was developed
	by the <organization>.  The name of the author may not be used to
	endorse or promote products derived from this software without
	specific prior written permission.

	THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

*/



public class WinLossStates {

	/** An array of these is used in creating the WIN and LOSS tables. */
	private class State {

		private int wins;
		
		private int runs;
		
		private double value;

	}
	
	public int wls_State_Best;			// The best state (Eg. 14/14)
	public int wls_EndScale;			// The current end of scale (1 .. 21) set by BuildTables()
	public int wls_binThresh_1div2;		// Binary value of 1/2	(0/1 if endscale < 2)
	
	public int[] WIN;
	public int[] LOSS;					// The tables. It is also safe to use an 8 bit unsigned

	private State[] states;

	public WinLossStates ()
	{
		wls_State_Best		= 0;
		wls_EndScale		= 0;
		wls_binThresh_1div2	= 0;
		
		WIN  = new int [256];
		LOSS = new int [256];	
		
		states = new State[256];
		for (int i = 0; i < states.length; i++) {
			states[i] = new State();
		}
	};


	public void BuildTables(int EndScale, int IniWins, int IniVisits)
	{
		int n, i, j;

		n = 0;
		for (i = 0; i<= EndScale; i++)
		{
			for (j = 0; j <= i; j++)
			{
				states[n].wins   = j;
				states[n].runs = i;

				if (i == 0) states[n].value = -9.9999;
				else 
				{
					if ((1.0*states[n].wins)/states[n].runs > 0.4999) states[n].value = AgrestiCoullLB(states[n].wins, states[n].runs);
					else 										  states[n].value = -1 + AgrestiCoullUB(states[n].wins, states[n].runs);
				};

				n++;
			}
		};

		QuickSort(0, n - 1);

		wls_State_Best = n - 1;
		wls_EndScale   = EndScale;

		i = FindIdx(IniWins + 1, IniVisits + 1);
		if (i < 0) i = FindIdx(1, 1);

		WIN[0] = i;

		i = FindIdx(IniWins, IniVisits + 1);
		if (i < 0) i = FindIdx(0, 1);

		LOSS[0] = i;

		// Build wls_WIN[] & wls_LOSS[]

		for (i = 1; i <= wls_State_Best; i++)
		{
			j = FindIdx(states[i].wins + 1, states[i].runs + 1);
			if (j > 0)	WIN[i] = j;
			else		WIN[i] = SaturatedIdx(states[i].wins, states[i].runs, 1);

			j = FindIdx(states[i].wins, states[i].runs + 1);
			if (j > 0)	LOSS[i] = j;
			else		LOSS[i] = SaturatedIdx(states[i].wins, states[i].runs, 0);
		};

		if (EndScale > 1) wls_binThresh_1div2 = FindIdx(1, 2);
		else			  wls_binThresh_1div2 = FindIdx(0, 1);
	};


	private void QuickSort (int l, int r)
	{
		int i, j;
		double tm;
		int ci;				// NOTE to Java users: This could more elegantly be defined by using a record type (see C++ and Pascal implementations)
		double cd;

		i  = l;
		j  = r;
		tm = states[(l + r)/2].value;

		do
		{
			while (states[i].value < tm) i++;
			while (states[j].value > tm) j--;

			if (i <= j)
			{
				if (i < j)
				{
					ci			= states[i].wins;		// NOTE to Java users: There must be a nicer way to do this exchange (see C++ and Pascal implementations)
					states[i].wins = states[j].wins;
					states[j].wins = ci;

					ci			  = states[i].runs;
					states[i].runs = states[j].runs;
					states[j].runs = ci;

					cd		 = states[i].value;
					states[i].value = states[j].value;
					states[j].value = cd;
				};

				i++;
				j--;
			}
		} while (i <= j);

		if (l < j) QuickSort(l, j);
		if (i < r) QuickSort(i, r);
	};


	private int FindIdx (int wins, int visits)
	{
		int i = wls_State_Best;

		do
		{
			if ((states[i].wins == wins) && (states[i].runs == visits)) return i;

			i--;
		} while (i >= 0);

		return i;
	};
	

	private int SaturatedIdx (int owins, int ovisits, int win)
	{
		int i, j, nv;

		if ((owins == 0) && (win == 0)) return 1;

		if ((owins == ovisits) && (win != 0)) return wls_State_Best;

		if (wls_EndScale < 4)
		{

			// Counter heuristic
	
			if (win != 0) return FindIdx(owins + 1, ovisits);
			else		  return FindIdx(owins - 1, ovisits);
		};

		// Confirmation heuristic

		j = FindIdx(owins, ovisits);
		
		// C++ :	nv = ovisits - int(0.5 + (ConfiH_K*ovisits*abs(double(owins)/ovisits - 0.5)));
				
		double d = owins;
		d = d/ovisits - 0.5;
		d = Math.abs(d);
		d = ConfiH_K*ovisits*d;

		nv = ovisits - (int)Math.round(d);

		if (win != 0)
		{
			for (i = j + 1; i <= wls_State_Best; i++)
			{
				if (states[i].runs == nv) return i;
			}
		}
		else 
		{
			for (i = j - 1; i >= 1; i--)
			{
				if (states[i].runs == nv) return i;
			}
		};

		return -1;	// This won't happen. Assertions have been removed from the multi-language version
	};
	

	private double AgrestiCoullLB(int wins, int visits)
	{
		double ZetaUpperQ = 0.674490;					// Upper quartile x of the N(0,1)
		double k2		  = ZetaUpperQ*ZetaUpperQ;
		double xe, ne, p, pq;

		xe = wins + 0.5*k2;
		ne = visits + k2;

	    p  = xe/ne;
		pq = (1 - p)*p;

		return p - ZetaUpperQ*Math.exp(0.5*(Math.log(pq) - Math.log(visits)));
	};
	

	private double AgrestiCoullUB(int wins, int visits)
	{
		double ZetaUpperQ = 0.674490;					// Upper quartile x of the N(0,1)
		double k2		  = ZetaUpperQ*ZetaUpperQ;
		double xe, ne, p, pq;

		xe = wins + 0.5*k2;
		ne = visits + k2;

	    p  = xe/ne;
	    pq = (1 - p)*p;

		return p + ZetaUpperQ*Math.exp(0.5*(Math.log(pq) - Math.log(visits)));
	};
	
	
	private double ConfiH_K = 1.3;		// Optimal value for End of Scale = 21

/*	These results are obtained for different values of ConfiH_K

JPS heuristic K                         : 0.90
SD (residuals) based on 20 draws        : 0.0900�0.0155 (N = 25000)
SD (residuals) based on ~200 draws      : 0.1084�0.0147 (N = 25000)
Spearman rank correlation on 20 draws   : 0.9625�0.0156 (N = 25000)
Spearman rank correlation on ~200 draws : 0.9742�0.0105 (N = 25000)

JPS heuristic K                         : 1.00
SD (residuals) based on 20 draws        : 0.0900�0.0155 (N = 25000)
SD (residuals) based on ~200 draws      : 0.0964�0.0141 (N = 25000)
Spearman rank correlation on 20 draws   : 0.9625�0.0156 (N = 25000)
Spearman rank correlation on ~200 draws : 0.9761�0.0098 (N = 25000)

JPS heuristic K                         : 1.10
SD (residuals) based on 20 draws        : 0.0900�0.0155 (N = 25000)
SD (residuals) based on ~200 draws      : 0.0970�0.0140 (N = 25000)
Spearman rank correlation on 20 draws   : 0.9625�0.0156 (N = 25000)
Spearman rank correlation on ~200 draws : 0.9753�0.0095 (N = 25000)

JPS heuristic K                         : 1.20
SD (residuals) based on 20 draws        : 0.0900�0.0155 (N = 25000)
SD (residuals) based on ~200 draws      : 0.0881�0.0132 (N = 25000)
Spearman rank correlation on 20 draws   : 0.9625�0.0156 (N = 25000)
Spearman rank correlation on ~200 draws : 0.9759�0.0095 (N = 25000)

JPS heuristic K                         : 1.30
SD (residuals) based on 20 draws        : 0.0900�0.0155 (N = 25000)
SD (residuals) based on ~200 draws      : 0.0817�0.0125 (N = 25000)
Spearman rank correlation on 20 draws   : 0.9625�0.0156 (N = 25000)
Spearman rank correlation on ~200 draws : 0.9759�0.0097 (N = 25000)

JPS heuristic K                         : 1.40
SD (residuals) based on 20 draws        : 0.0900�0.0155 (N = 25000)
SD (residuals) based on ~200 draws      : 0.0838�0.0129 (N = 25000)
Spearman rank correlation on 20 draws   : 0.9625�0.0156 (N = 25000)
Spearman rank correlation on ~200 draws : 0.9746�0.0102 (N = 25000)			*/

};
