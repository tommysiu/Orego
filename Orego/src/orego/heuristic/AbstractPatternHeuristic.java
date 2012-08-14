package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public abstract class AbstractPatternHeuristic extends Heuristic {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
	public static final BitVector[] BAD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	public static final BitVector[] GOOD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	/**
	 * Used by isPossibleNeighborhood().
	 */
	public static final char[] VALID_OFF_BOARD_PATTERNS = {
				diagramToNeighborhood("...\n. .\n..."),
				diagramToNeighborhood("*..\n* .\n*.."),
				diagramToNeighborhood("..*\n. *\n..*"),
				diagramToNeighborhood("***\n. .\n..."),
				diagramToNeighborhood("...\n. .\n***"),
				diagramToNeighborhood("***\n* .\n*.."),
				diagramToNeighborhood("***\n. *\n..*"),
				diagramToNeighborhood("*..\n* .\n***"),
				diagramToNeighborhood("..*\n. *\n***") };

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 * 
	 * @see orego.core.Coordinates#NEIGHBORS
	 */
	static {
		String[] colorSpecificPatterns = {
				"#OO#####", //6
				"OOO#####", //7
				".OO#####", //8
				"O.O#####", //10
				"OOOO####", //15
				"..OO####", //17
				"O..O####", //18
				"...O####", //19
				"#OO#O###", //30
				".OO#O###", //32
				"#.O#O###", //33
				"..O#O###", //35
				"##OOO###", //45
				"O#OOO###", //46
				".#OOO###", //47
				"..OOO###", //50
				"O#.OO###", //52
				".#.OO###", //53
				"O..OO###", //58
				"...OO###", //59
				"O#..O###", //61
				"OO..O###", //63
				".O..O###", //64
				"#OO#.###", //75
				"OOO#.###", //76
				".OO#.###", //77
				"..O#.###", //80
				"O#OO.###", //91
				".#OO.###", //92
				"..OO.###", //95
				"O#.O.###", //97
				".#.O.###", //98
				"#O.O.###", //99
				".O.O.###", //101
				"O..O.###", //103
				"...O.###", //104
				"O#...###", //106
				"OO...###", //108
				".O...###", //109
				"#OO#OO##", //120
				"#.O#OO##", //123
				"O##OOO##", //130
				".##OOO##", //131
				"#O#OOO##", //132
				"OO#OOO##", //133
				".O#OOO##", //134
				"#.#OOO##", //135
				"O.#OOO##", //136
				"..#OOO##", //137
				"#.OOOO##", //141
				"#..OOO##", //144
				"...OOO##", //146
				"#O#.OO##", //150
				"#OO.OO##", //156
				"#.O.OO##", //159
				"..O.OO##", //161
				"#...OO##", //162
				"#O##.O##", //168
				"#OO#.O##", //177
				"#O.#.O##", //186
				"O##O.O##", //193
				".##O.O##", //194
				"#O#O.O##", //195
				"OO#O.O##", //196
				".O#O.O##", //197
				"#.#O.O##", //198
				"O.#O.O##", //199
				"..#O.O##", //200
				"#OOO.O##", //204
				"#.OO.O##", //207
				"##.O.O##", //210
				"O#.O.O##", //211
				".#.O.O##", //212
				"#O.O.O##", //213
				"#..O.O##", //216
				"O..O.O##", //217
				"...O.O##", //218
				"#OO..O##", //231
				"#.O..O##", //234
				"#O...O##", //240
				"#OO#..##", //255
				".OO#..##", //257
				"O##O..##", //265
				".##O..##", //266
				"#O#O..##", //267
				"OO#O..##", //268
				".O#O..##", //269
				"O.#O..##", //271
				"..#O..##", //272
				"#OOO..##", //273
				"#.OO..##", //276
				"..OO..##", //278
				"#..O..##", //279
				"O..O..##", //280
				"...O..##", //281
				"#OO...##", //291
				".OO...##", //293
				"#.O...##", //294
				"*OO#**##", //303
				"*##O**##", //306
				"*O#O**##", //307
				"*.#O**##", //308
				"*OOO**##", //309
				"*..O**##", //311
				"*.O.**##", //316
				"O####OO#", //319
				"OO###OO#", //321
				"O#O##OO#", //324
				".#O##OO#", //325
				"#OO##OO#", //326
				"OOO##OO#", //327
				".OO##OO#", //328
				"#.O##OO#", //329
				"O.O##OO#", //330
				"..O##OO#", //331
				"OO.##OO#", //333
				"#..##OO#", //335
				"...##OO#", //337
				"OOOO#OO#", //338
				"..OO#OO#", //340
				"O..O#OO#", //342
				"...O#OO#", //343
				"....#OO#", //344
				"##O#OOO#", //351
				"O#O#OOO#", //352
				".#O#OOO#", //353
				"#OO#OOO#", //354
				"#.O#OOO#", //357
				"..O#OOO#", //359
				".#.#OOO#", //362
				"#O.#OOO#", //363
				"#..#OOO#", //366
				"##OOOOO#", //369
				"##.OOOO#", //375
				"O#.OOOO#", //376
				".#.OOOO#", //377
				"#..OOOO#", //381
				"##..OOO#", //384
				"O#..OOO#", //385
				".#..OOO#", //386
				"....OOO#", //389
				"##O#.OO#", //396
				"O#O#.OO#", //397
				".#O#.OO#", //398
				"#OO#.OO#", //399
				".OO#.OO#", //401
				"#.O#.OO#", //402
				"O#.#.OO#", //406
				"#O.#.OO#", //408
				"##OO.OO#", //414
				"O#OO.OO#", //415
				".#OO.OO#", //416
				"##.O.OO#", //420
				"O#.O.OO#", //421
				".#.O.OO#", //422
				"#O.O.OO#", //423
				"#..O.OO#", //426
				"O#...OO#", //430
				".#...OO#", //431
				".....OO#", //434
				"OO###.O#", //439
				"O#O##.O#", //444
				".#O##.O#", //445
				"#OO##.O#", //446
				"OOO##.O#", //447
				".OO##.O#", //448
				"#.O##.O#", //449
				"O.O##.O#", //450
				"..O##.O#", //451
				"OO.##.O#", //454
				"O.#O#.O#", //463
				"#.OO#.O#", //467
				"O.OO#.O#", //468
				"..OO#.O#", //469
				"O.O.#.O#", //477
				"..O.#.O#", //478
				"....#.O#", //479
				"##O#O.O#", //489
				"O#O#O.O#", //490
				".#O#O.O#", //491
				"#OO#O.O#", //492
				"#.O#O.O#", //495
				"..O#O.O#", //497
				"O#.#O.O#", //499
				"###OO.O#", //507
				"O##OO.O#", //508
				".##OO.O#", //509
				"##OOO.O#", //516
				".#OOO.O#", //518
				"##.OO.O#", //525
				".#.OO.O#", //527
				"O##.O.O#", //535
				".##.O.O#", //536
				"##O.O.O#", //543
				".#O.O.O#", //545
				"#OO.O.O#", //546
				"#.O.O.O#", //549
				"##..O.O#", //552
				"O#..O.O#", //553
				".#..O.O#", //554
				"O#O#..O#", //571
				".#O#..O#", //572
				"#OO#..O#", //573
				".OO#..O#", //575
				"#.O#..O#", //576
				"..O#..O#", //578
				"O#.#..O#", //580
				"O##O..O#", //589
				".##O..O#", //590
				"##OO..O#", //597
				"O#OO..O#", //598
				".#OO..O#", //599
				"#OOO..O#", //600
				"#.OO..O#", //603
				"##.O..O#", //606
				"O#.O..O#", //607
				".#.O..O#", //608
				"O##...O#", //616
				"O#O...O#", //625
				"#OO...O#", //627
				"#.O...O#", //630
				"O#....O#", //634
				".#....O#", //635
				"*#O#**O#", //645
				"*OO#**O#", //646
				"*.O#**O#", //647
				"*##O**O#", //651
				"*#.O**O#", //657
				"#OO##..#", //677
				"OOO##..#", //678
				".OO##..#", //679
				"OO.##..#", //684
				"..OO#..#", //691
				"O..O#..#", //693
				"...O#..#", //694
				"##O#O..#", //702
				"#OO#O..#", //705
				"#.O#O..#", //708
				"##OOO..#", //720
				"O#OOO..#", //721
				".#OOO..#", //722
				"##.OO..#", //726
				"O#.OO..#", //727
				".#.OO..#", //728
				"#O.OO..#", //729
				"#..OO..#", //732
				"#OO#...#", //750
				".OO#...#", //752
				"#.O#...#", //753
				"..O#...#", //755
				"##OO...#", //765
				"O#OO...#", //766
				".#OO...#", //767
				"..OO...#", //770
				"O#.O...#", //772
				".#.O...#", //773
				"#O.O...#", //774
				"O..O...#", //778
				"...O...#", //779
				"*OO#**.#", //790
				"*##O**.#", //795
				"*#OO**.#", //798
				"*#.O**.#", //801
				"*..O**.#", //803
				"*.O.**.#", //809
				"**OO***#", //816
				"####OOOO", //819
				"..##OOOO", //824
				"#OO#OOOO", //825
				"#.O#OOOO", //828
				"..O#OOOO", //830
				"#..#OOOO", //831
				"...#OOOO", //833
				"O###.OOO", //841
				"OO##.OOO", //843
				".O##.OOO", //844
				"..##.OOO", //845
				"##O#.OOO", //846
				"O#O#.OOO", //847
				".#O#.OOO", //848
				"#OO#.OOO", //849
				"#.O#.OOO", //852
				"##.#.OOO", //855
				"O#.#.OOO", //856
				".#.#.OOO", //857
				"#O.#.OOO", //858
				".O.#.OOO", //860
				"#..#.OOO", //861
				"...#.OOO", //863
				".#...OOO", //881
				"O###..OO", //886
				"#O##..OO", //888
				"OO##..OO", //889
				".O##..OO", //890
				"O.##..OO", //892
				"..##..OO", //893
				"#OO#..OO", //894
				"#.O#..OO", //897
				"..O#..OO", //899
				"#..#..OO", //900
				"O..#..OO", //901
				"...#..OO", //902
				"O##O..OO", //904
				".##O..OO", //905
				"O##...OO", //922
				"O.#...OO", //928
				"*O##**OO", //940
				"*OO#**OO", //942
				"*.O#**OO", //943
				"*..#**OO", //944
				"O###O..O", //958
				"OO##O..O", //960
				".O##O..O", //961
				"..##O..O", //962
				"#OO#O..O", //965
				"#.O#O..O", //968
				"#..#O..O", //974
				"...#O..O", //976
				"....O..O", //983
				"O###...O", //985
				"OO##...O", //987
				".O##...O", //988
				"#OO#...O", //993
				"#.O#...O", //996
				"#O.#...O", //1002
				".O.#...O", //1004
				"...#...O", //1007
				"*OO#**.O", //1033
				"*O.#**.O", //1036
				"#OO#....", //1068
				".OO#....", //1070
				"#.O#....", //1071
				"........", //1082
				"*OO#**..", //1086
				"*##O**..", //1089
		};
//		String[] colorSpecificPatterns = {
//				"O...#O??", // Hane4
//				"#??*?O**", // Edge3
//				"O?+*?#**", // Edge4
//				"O#O*?#**", // Edge5	
//		};
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		for (int i = 0; i < BLACK_GOOD_PATTERNS.length; i++) {
			BLACK_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], BLACK);
			WHITE_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], WHITE);
		}
//		String[] colorIndependentPatterns = {
//				"O..?##??", // Hane1
//				"O...#.??", // Hane2
//				"O#..#???", // Hane3
//				"#OO+??++", // Cut2
//				".O?*#?**", // Edge1
//				"#oO*??**", // Edge2				
//		};
//		Pattern[] INDEPENDENT_GOOD_PATTERNS = new Pattern[colorIndependentPatterns.length + 1];			
//		for (int i = 0; i < colorIndependentPatterns.length; i++) {
//			INDEPENDENT_GOOD_PATTERNS[i] = new SimplePattern(colorIndependentPatterns[i]);
//		}
//		INDEPENDENT_GOOD_PATTERNS[INDEPENDENT_GOOD_PATTERNS.length - 1] = new Cut1Pattern();
		String[] badPatterns = {
				"########", //0
				"O#######", //1
				".#######", //2
				".O######", //4
				"..######", //5
				"#.O#####", //9
				"#..#####", //12
				"...#####", //14
				".OOO####", //16
				"....####", //20
				"####O###", //21
				"O###O###", //22
				".###O###", //23
				"OO##O###", //24
				".O##O###", //25
				"..##O###", //26
				"O#O#O###", //28
				"OOO#O###", //31
				"O.O#O###", //34
				"##.#O###", //36
				"O#.#O###", //37
				".#.#O###", //38
				"#O.#O###", //39
				"OO.#O###", //40
				".O.#O###", //41
				"#..#O###", //42
				"O..#O###", //43
				"...#O###", //44
				"OOOOO###", //48
				".OOOO###", //49
				"OO.OO###", //55
				"##..O###", //60
				"O#..O###", //61
				".#..O###", //62
				"....O###", //65
				"####.###", //66
				"O###.###", //67
				".###.###", //68
				"OO##.###", //69
				".O##.###", //70
				"..##.###", //71
				"##O#.###", //72
				".#O#.###", //74
				"##.#.###", //81
				"O#.#.###", //82
				".#.#.###", //83
				"#O.#.###", //84
				".O.#.###", //86
				"#..#.###", //87
				"O..#.###", //88
				"...#.###", //89
				".OOO.###", //94
				"##.O.###", //96
				"OO.O.###", //100
				"#..O.###", //102
				"##...###", //105
				"####OO##", //111
				"O###OO##", //112
				".###OO##", //113
				"OO##OO##", //115
				".O##OO##", //116
				"#.##OO##", //117
				"O.##OO##", //118
				"..##OO##", //119
				"#OO#OO##", //120
				"OOO#OO##", //121
				".OO#OO##", //122
				"O.O#OO##", //124
				"..O#OO##", //125
				"O..#OO##", //127
				"...#OO##", //128
				"OOOOOO##", //139
				".OOOOO##", //140
				"O.OOOO##", //142
				"..OOOO##", //143
				"###.OO##", //147
				"O##.OO##", //148
				".##.OO##", //149
				"OO#.OO##", //151
				"#.#.OO##", //153
				"O.#.OO##", //154
				"..#.OO##", //155
				"OOO.OO##", //157
				".OO.OO##", //158
				"O.O.OO##", //160
				"O...OO##", //163
				"####.O##", //165
				"O###.O##", //166
				".###.O##", //167
				"OO##.O##", //169
				".O##.O##", //170
				"#.##.O##", //171
				"O.##.O##", //172
				"..##.O##", //173
				"##O#.O##", //174
				"O#O#.O##", //175
				".#O#.O##", //176
				"OOO#.O##", //178
				"O.O#.O##", //181
				"..O#.O##", //182
				"##.#.O##", //183
				".#.#.O##", //185
				"OO.#.O##", //187
				".O.#.O##", //188
				"#..#.O##", //189
				"O..#.O##", //190
				"...#.O##", //191
				"###O.O##", //192
				".OOO.O##", //206
				"O.OO.O##", //208
				"###..O##", //219
				"O##..O##", //220
				".##..O##", //221
				"#.#..O##", //225
				"O.#..O##", //226
				"..#..O##", //227
				"O.O..O##", //235
				"##...O##", //237
				"O#...O##", //238
				".#...O##", //239
				"O....O##", //244
				"####..##", //246
				"O###..##", //247
				".###..##", //248
				"#O##..##", //249
				"OO##..##", //250
				".O##..##", //251
				"#.##..##", //252
				"O.##..##", //253
				"O.O#..##", //259
				"..O#..##", //260
				"#..#..##", //261
				"O..#..##", //262
				"...#..##", //263
				".OOO..##", //275
				"O.OO..##", //277
				"###...##", //282
				"O##...##", //283
				".##...##", //284
				"#.#...##", //288
				"O.#...##", //289
				"..#...##", //290
				"OOO...##", //292
				"#.....##", //297
				"......##", //299
				"*###**##", //300
				"*O##**##", //301
				"*.##**##", //302
				"*.O#**##", //304
				"*..#**##", //305
				"*.OO**##", //310
				"*##.**##", //312
				"*O#.**##", //313
				"*.#.**##", //314
				"*...**##", //317
				".####OO#", //320
				"..###OO#", //323
				".OOO#OO#", //339
				"OO##OOO#", //348
				".O##OOO#", //349
				"OOO#OOO#", //355
				".OO#OOO#", //356
				"O.O#OOO#", //358
				"OO.#OOO#", //364
				".O.#OOO#", //365
				"O..#OOO#", //367
				".OOOOOO#", //373
				"..OOOOO#", //374
				"OO.OOOO#", //379
				".O.OOOO#", //380
				"O..OOOO#", //382
				"...OOOO#", //383
				"OO..OOO#", //387
				".O..OOO#", //388
				"####.OO#", //390
				".###.OO#", //392
				"OO##.OO#", //393
				".O##.OO#", //394
				"OOO#.OO#", //400
				"##.#.OO#", //405
				"OO.#.OO#", //409
				".OOO.OO#", //418
				"OO.O.OO#", //424
				".O.O.OO#", //425
				"O..O.OO#", //427
				"OO...OO#", //432
				"#####.O#", //435
				".####.O#", //437
				"#O###.O#", //438
				"#.###.O#", //441
				"..###.O#", //443
				".#.##.O#", //452
				"#..##.O#", //456
				"#O#O#.O#", //459
				"#.#O#.O#", //462
				"O.OO#.O#", //468
				".O.O#.O#", //470
				"#.#.#.O#", //474
				"..#.#.O#", //476
				"####O.O#", //480
				"#O##O.O#", //483
				"OO##O.O#", //484
				".O##O.O#", //485
				"#.##O.O#", //486
				"O.##O.O#", //487
				"..##O.O#", //488
				"OOO#O.O#", //493
				"OO.#O.O#", //502
				".O.#O.O#", //503
				"#..#O.O#", //504
				"O..#O.O#", //505
				"...#O.O#", //506
				"#O#OO.O#", //510
				"OO#OO.O#", //511
				".O#OO.O#", //512
				"#.#OO.O#", //513
				"O.#OO.O#", //514
				".OOOO.O#", //521
				"O.OOO.O#", //523
				"..OOO.O#", //524
				"OO.OO.O#", //529
				".O.OO.O#", //530
				"O..OO.O#", //532
				"...OO.O#", //533
				"###.O.O#", //534
				"#O#.O.O#", //537
				"OO#.O.O#", //538
				".O#.O.O#", //539
				"#.#.O.O#", //540
				"O.#.O.O#", //541
				"..#.O.O#", //542
				"OOO.O.O#", //547
				".OO.O.O#", //548
				"O.O.O.O#", //550
				"OO..O.O#", //556
				".O..O.O#", //557
				"O...O.O#", //559
				"####..O#", //561
				".###..O#", //563
				"#O##..O#", //564
				"OO##..O#", //565
				".O##..O#", //566
				"#.##..O#", //567
				"..##..O#", //569
				"##.#..O#", //579
				"OO.#..O#", //583
				".O.#..O#", //584
				"#..#..O#", //585
				"O..#..O#", //586
				"...#..O#", //587
				"###O..O#", //588
				"#O#O..O#", //591
				"OO#O..O#", //592
				".O#O..O#", //593
				"#.#O..O#", //594
				"OOOO..O#", //601
				".OOO..O#", //602
				"O.OO..O#", //604
				"OO.O..O#", //610
				".O.O..O#", //611
				"O..O..O#", //613
				"###...O#", //615
				".##...O#", //617
				"#O#...O#", //618
				"OO#...O#", //619
				".O#...O#", //620
				"#.#...O#", //621
				"..#...O#", //623
				"OOO...O#", //628
				".OO...O#", //629
				"O.O...O#", //631
				"OO....O#", //637
				".O....O#", //638
				"*###**O#", //642
				"*O##**O#", //643
				"*O.#**O#", //649
				"*..#**O#", //650
				"*O#O**O#", //652
				"*OOO**O#", //655
				"*.OO**O#", //656
				"*O.O**O#", //658
				"*O#.**O#", //661
				"*.#.**O#", //662
				"*OO.**O#", //664
				"*O..**O#", //667
				"#####..#", //669
				"O####..#", //670
				".####..#", //671
				"..###..#", //674
				"O#O##..#", //675
				".#O##..#", //676
				".#.##..#", //683
				"#..##..#", //686
				"...##..#", //688
				".OOO#..#", //690
				"####O..#", //696
				"O###O..#", //697
				".###O..#", //698
				"OO##O..#", //699
				".O##O..#", //700
				"..##O..#", //701
				"O#O#O..#", //703
				"OOO#O..#", //706
				"##.#O..#", //711
				"O#.#O..#", //712
				".#.#O..#", //713
				"OO.#O..#", //715
				".O.#O..#", //716
				"#..#O..#", //717
				"O..#O..#", //718
				"...#O..#", //719
				"OOOOO..#", //723
				".OOOO..#", //724
				"OO.OO..#", //730
				".O.OO..#", //731
				"O..OO..#", //733
				"OO..O..#", //738
				".O..O..#", //739
				"####...#", //741
				"O###...#", //742
				".###...#", //743
				"OO##...#", //744
				".O##...#", //745
				"..##...#", //746
				"O#O#...#", //748
				".#O#...#", //749
				"##.#...#", //756
				"O#.#...#", //757
				".#.#...#", //758
				"OO.#...#", //760
				".O.#...#", //761
				"O..#...#", //763
				"...#...#", //764
				".OOO...#", //769
				"OO.O...#", //775
				".O.O...#", //776
				"##.....#", //780
				"OO.....#", //783
				"*###**.#", //786
				"*O##**.#", //787
				"*.##**.#", //788
				"*#O#**.#", //789
				"*#.#**.#", //792
				"*..#**.#", //794
				"*OOO**.#", //799
				"*O.O**.#", //802
				"*##.**.#", //804
				"*O#.**.#", //805
				"*OO.**.#", //808
				"*#..**.#", //810
				"*...**.#", //812
				"**##***#", //813
				"**.#***#", //815
				"OO##OOOO", //822
				"OOO#OOOO", //826
				".OO#OOOO", //827
				"O.O#OOOO", //829
				".OOOOOOO", //835
				"..OOOOOO", //836
				"O..OOOOO", //837
				"...OOOOO", //838
				"OOO#.OOO", //850
				"O.O#.OOO", //853
				"..O#.OOO", //854
				"OO.#.OOO", //859
				"##OO.OOO", //864
				"O#OO.OOO", //865
				".#OO.OOO", //866
				".OOO.OOO", //868
				"..OO.OOO", //869
				"##.O.OOO", //870
				"O#.O.OOO", //871
				".#.O.OOO", //872
				"#O.O.OOO", //873
				"OO.O.OOO", //874
				".O.O.OOO", //875
				"#..O.OOO", //876
				"O..O.OOO", //877
				"...O.OOO", //878
				"OO...OOO", //882
				".O...OOO", //883
				"OOO#..OO", //895
				"###O..OO", //903
				"#O#O..OO", //906
				"OO#O..OO", //907
				".O#O..OO", //908
				"#.#O..OO", //909
				"..#O..OO", //911
				"#OOO..OO", //912
				".OOO..OO", //914
				"#.OO..OO", //915
				"O.OO..OO", //916
				"..OO..OO", //917
				"#..O..OO", //918
				"O..O..OO", //919
				"...O..OO", //920
				"###...OO", //921
				"#O#...OO", //924
				".O#...OO", //926
				"#.#...OO", //927
				"#OO...OO", //930
				"OOO...OO", //931
				".OO...OO", //932
				"#.O...OO", //933
				"O.O...OO", //934
				"..O...OO", //935
				"#.....OO", //936
				"*O#O**OO", //946
				"*.#O**OO", //947
				"*.OO**OO", //949
				"*..O**OO", //950
				"*O#.**OO", //952
				"*OO.**OO", //954
				"*.O.**OO", //955
				"*...**OO", //956
				"####O..O", //957
				".###O..O", //959
				"O#O#O..O", //963
				"OOO#O..O", //966
				".OOOO..O", //978
				"..OOO..O", //979
				"O..OO..O", //981
				"...OO..O", //982
				"####...O", //984
				".###...O", //986
				"O#O#...O", //991
				".#O#...O", //992
				"OOO#...O", //994
				"O.O#...O", //997
				"##.#...O", //999
				".#.#...O", //1001
				"##OO...O", //1008
				"O#OO...O", //1009
				".#OO...O", //1010
				".OOO...O", //1012
				"..OO...O", //1013
				"##.O...O", //1014
				"#O.O...O", //1017
				"OO.O...O", //1018
				".O.O...O", //1019
				"#..O...O", //1020
				"O..O...O", //1021
				"...O...O", //1022
				"##.....O", //1023
				"OO.....O", //1026
				"*###**.O", //1029
				"*#.#**.O", //1035
				"*#OO**.O", //1041
				"*.OO**.O", //1043
				"*O.O**.O", //1045
				"*..O**.O", //1046
				"*##.**.O", //1047
				"*#O.**.O", //1050
				"*OO.**.O", //1051
				"*.O.**.O", //1052
				"*#..**.O", //1053
				"**##***O", //1056
				"**O#***O", //1057
				"**.O***O", //1060
				"####....", //1062
				"O###....", //1063
				".###....", //1064
				"OO##....", //1065
				".O##....", //1066
				"..##....", //1067
				"O.O#....", //1072
				"#..#....", //1074
				".OOO....", //1078
				"..OO....", //1079
				"O..O....", //1080
				"*###**..", //1083
				"*O##**..", //1084
				"*.##**..", //1085
				"*..#**..", //1088
				"*O#O**..", //1090
				"*.OO**..", //1093
				"*..O**..", //1094
				"*##.**..", //1095
				"*.#.**..", //1097
				"*OO.**..", //1098
				"*.O.**..", //1099
				"*...**..", //1100
				"**##***.", //1101
				"**O#***.", //1102
				"**.#***.", //1103
				"**.O***.", //1105
				"**..***.", //1106
			};
		Pattern[] BLACK_BAD_PATTERNS = new Pattern[badPatterns.length];
		Pattern[] WHITE_BAD_PATTERNS = new Pattern[badPatterns.length];
		for (int i = 0; i < BLACK_BAD_PATTERNS.length; i++) {
			BLACK_BAD_PATTERNS[i] = new ColorSpecificPattern(badPatterns[i], BLACK);
			WHITE_BAD_PATTERNS[i] = new ColorSpecificPattern(badPatterns[i], WHITE);
		}
		// Find all good neighborhoods, i.e., neighborhoods where a player
		// should play.
		// Note that i has to be an int, rather than a char, because
		// otherwise incrementing it after Character.MAX_VALUE would
		// return it to 0, resulting in an infinite loop.
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (Pattern pattern : BLACK_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
//			for (Pattern pattern : INDEPENDENT_GOOD_PATTERNS) {
//				if (pattern.matches((char) i)) {
//					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
//					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
//				}
//			}
			for (Pattern pattern : BLACK_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}	
		}
	}

	/**
	 * Returns true if the the specified 3x3 neighborhood can possibly occur.
	 * Neighborhoods are impossible if, for example, there are non-contiguous
	 * off-board points.
	 */
	public static boolean isPossibleNeighborhood(char neighborhood) {
		int mask = 0x3;
		// Replace black and white with vacant, leaving only
		// vacant and off-board colors
		for (int i = 0; i < 16; i += 2) {
			if ((neighborhood >>> i & mask) != OFF_BOARD_COLOR) {
				neighborhood &= ~(mask << i);
				neighborhood |= VACANT << i;
			}
		}
		// Verify that the resulting pattern is valid
		assert VALID_OFF_BOARD_PATTERNS != null;
		for (char v : VALID_OFF_BOARD_PATTERNS) {
			if (neighborhood == v) {
				return true;
			}
		}
		return false;
	}

	public AbstractPatternHeuristic(int weight) {
		super(weight);
	}

}
