package com.ltd.lifesearch_xa;

public class colorPalette {
	private int mColNumber=256;//30;         //��ɫ����ɫ��
	private int mCurrentColPalIndex=0;     //��ǰѡ��ĵ�ɫ������
	private int mBackColor = 0xffffffff;
	private int mFillColor = 0xff000000;
	private int mHighColpalNumber=3;
	
	//�о��ȵ�ɫ����ɫ����
	private int mMiddleColNumber=129;         //��ɫ����ɫ��
	private int[][][] mMiddleColPal =
	{
		//��--��--��
		{
			{120,0,120},      {125,0,125},    {130,0,130},    {135,0,135},     {140,0,140},
			{145,0,145},      {150,0,150},    {155,0,155},    {160,0,160},     {165,0,165},
			{170,0,170},      {175,0,175},    {180,0,180},    {185,0,185},     {190,0,190},
			{195,0,195},      {200,0,200},    {205,0,205},    {210,0,210},     {215,0,215},
			{220,0,220},      {225,0,225},    {230,0,230},    {235,0,235},     {240,0,240},
			{226,16,226},     {212,32,212},   {198,48,198},   {184,64,184},    {170,80,170},
			{156,96,156},     {142,112,142},  {128,128,128},  {132,132,132},   {136, 136, 136},
			{140,140,140},    {144,144,144},  {148,148,148},  {152,152,152},   {156, 156, 156},
			{160,160,160},    {164,164,164},  {168,168,168},  {172,172,172},   {176, 176, 176},
			{180,180,180},    {184,184,184},  {188,188,188},  {192,192,192},   {196, 196, 196},
			{200,200,200},    {204,204,204},  {208,208,208},  {212,212,212},   {216, 216, 216},
			{220,220,220},    {224,224,224},  {228,228,228},  {232,232,232},   {236, 236, 236},
			{240,240,240},    {244,244,244},  {248,248,248},  {252,252,252},   {255, 255, 255},
			{252,252,252},    {248,248,248},  {244,244,244},  {240,240,240},   {236, 236, 236},
			{232,232,232},    {228,228,228},  {224,224,224},  {220,220,220},   {216, 216, 216},
			{212,212,212},    {208,208,208},  {204,204,204},  {200,200,200},   {196, 196, 196},
			{192,192,192},    {188,188,188},  {184,184,184},  {180,180,180},   {176, 176, 176},
			{172,172,172},    {168,168,168},  {164,164,164},  {160,160,160},   {156, 156, 156},
			{152,152,152},    {148,148,148},  {144,144,144},  {140,140,140},   {136,136,136},
			{132,132,132},    {128,128,128},  {117,122,142},  {106,116,156},   {95, 110, 170},
			{84,104,184},     {73, 98, 198},  {62, 92, 212},  {51,  86,226},   {40,  80, 240},
			{38, 75, 238},    {35, 70, 235},  {32, 65, 232},  {30, 60, 230},   {28,  55, 228},
			{25, 50, 225},    {22, 45, 222},  {20, 40, 220},  {18, 35, 218},   {15,  30, 215},
		    {12, 25, 212},    {10, 20, 210},  {8,  15, 208},  {5,  10, 205},   {2,   5, 202},
			{0,   0, 200},    {0,   0, 195},  {0,   0, 190},  {0,  0 ,185},    {0,   0, 180},
		    {0,   0, 175},    {0,   0, 170},  {0,   0, 165},  {0,   0, 160},
		},
		///������ɫ��
		{
			{0,0,127}, {0,0,135}, {0,0,143}, {0,0,151}, {0,0,159},
			{0,0,167}, {0,0,175}, {0,0,183}, {0,0,191}, {0,0,199},
			{0,0,207}, {0,0,215}, {0,0,223}, {0,0,231}, {0,0,239},
			{0,0,247}, {0,0,255}, {0,8,255}, {0,16,255},{0,24,255},
			{0,32,255},{0,40,255},{0,48,255},{0,56,255},{0,64,255},
			{0,72,255},{0,80,255},{0,88,255},{0,96,255},{0,104,255},
			{0,112,255},{0,120,255},{0,128,255},{0,136,255},{0,143,255},
			{0,151,255},{0,159,255},{0,167,255},{0,175,255},{0,183,255},
			{0,191,255},{0,199,255},{0,207,255},{0,215,255},{0,223,255},
			{0,231,255},{0,239,255},{0,247,255},{0,255,255},{8,255,255},
			{16,255,255},{24,255,247},{32,255,223},{40,255,215},{48,255,207},
			{56,255,199},{64,255,191},{72,255,183},{80,255,175},{88,255,167},
			{96,255,159},{104,255,151},{112,255,143},{120,255,135},{128,255,128},
			{136,255,120},{143,255,112},{151,255,104},{159,255,96},{167,255,88},
			{175,255,80},{183,255,72},{191,255,64},{199,255,56},{207,255,48},
			{215,255,40},{223,255,32},{231,255,24},{239,255,16},{247,255,8},
			{255,255,0},{255,247,0},{255,239,0},{255,231,0},{255,223,0},
			{255,215,0},{255,207,0},{255,199,0},{255,191,0},{255,183,0},
			{255,175,0},{255,167,0},{255,159,0},{255,151,0},{255,143,0},
			{255,136,0},{255,128,0},{255,120,0},{255,112,0},{255,104,0},
			{255,96,0},{255,88,0},{255,80,0},{255,72,0},{255,64,0},
			{255,56,0},{255,48,0},{255,40,0},{255,32,0},{255,24,0},
			{255,16,0},{255,8,0},{255,0,0},{247,0,0},{239,0,0},
			{231,0,0},{223,0,0},{215,0,0},{207,0,0},{199,0,0},
			{191,0,0},{183,0,0},{175,0,0},{167,0,0},{159,0,0},
			{151,0,0},{143,0,0},{135,0,0},{128,0,0},
		},
		
	};
	private int[][][] mLowColPal =
	{
		{
			//�Ҷȵ�ɫ��
			{0,0,0},     {8,8,8},      {17,17,17},   {26,26,26},   {35,35,35},
			{44,44,44},  {53,53,53},   {62,62,62},   {71,71,71},   {80,80,80},
			{89,89,89},  {98,98,98},   {107,107,107},{116,116,116},{125,125,125},
			{134,134,134},{143,143,143},{152,152,152},{161,161,161},{170,170,170},
			{178,178,178},{187,187,187},{196,196,196},{205,205,205},{214,214,214},
			{223,223,223},{232,232,232},{241,241,241},{250,250,250},{255,255,255},	
		},
	};
	//��ɫ����
	private int[][][] mColPal =      
	{
		///�Ҷ�
		/*
		{
			{0,0,0},   {1,1,1},     {2,2,2},      {3,3,3},      {4,4,4},         {5,5,5},
			{6,6,6},   {7,7,7},     {8,8,8},      {9,9,9},      {10,10,10},      {11,11,11},
			{12,12,12},{13,13,13},  {14,14,14},   {15,15,15},   {16,16,16},      {17,17,17},
			{18,18,18},{19,19,19},  {20,20,20},   {21,21,21},   {22,22,22},     {23,23,23},
			{24,24,24},	{25,25,25},	{26,26,26},	{27,27,27},	{28,28,28},	{29,29,29},
			{30,30,30},	{31,31,31},	{32,32,32},	{33,33,33},	{34,34,34},	{35,35,35},
			{36,36,36},	{37,37,37},	{38,38,38},	{39,39,39},	{40,40,40},	{41,41,41},
			{42,42,42},	{42,42,42},	{43,43,43},	{44,44,44},	{45,45,45},	{46,46,46},
			{47,47,47},	{48,48,48},	{49,49,49},	{50,50,50},	{51,51,51},	{52,52,52},
			{53,53,53},	{54,54,54},	{55,55,55},	{56,56,56},	{57,57,57},	{58,58,58},
			{59,59,59},	{60,60,60},	{61,61,61},	{62,62,62},	{63,64,63},	{64,65,64},
			{65,66,65},	{66,67,66},	{67,68,67},	{68,69,68},	{69,70,69},	{70,71,70},
			{71,72,71},	{72,73,72},	{73,74,73},	{74,75,74},	{75,76,75},	{76,77,76},
			{77,78,77},	{78,79,78},	{79,80,79},	{80,81,80},	{81,82,81},	{82,83,82},
			{83,84,83},	{84,85,84},	{85,86,85},	{86,87,86},	{87,88,87},	{88,89,88},
			{89,90,89},	{90,91,90},	{91,92,91},	{92,93,92},	{93,94,93},	{94,95,94},
			{95,96,95},	{96,97,96},	{97,98,97},	{98,99,98},	{99,100,99},	{100,101,100},
			{101,102,101},	{102,103,102},	{103,104,103},	{104,105,104},	{105,106,105},	{106,107,106},
			{107,108,108},	{108,109,109},	{109,110,110},	{110,111,111},	{111,112,112},	{112,113,113},
			{113,114,114},	{114,115,115},	{115,116,116},	{116,117,117},	{117,118,118},	{118,119,119},
			{119,120,120},	{120,121,121},	{121,122,122},	{122,123,123},	{123,124,124},	{124,125,125},
			{125,126,126},	{126,127,127},	{126,127,127},	{127,128,128},	{128,129,129},	{129,130,130},
			{130,131,131},	{131,132,132},	{132,133,133},	{133,134,134},	{134,135,135},	{135,136,136},
			{136,137,137},	{137,138,138},	{138,139,139},	{139,140,140},	{140,141,141},	{141,142,142},
			{142,143,143},	{143,144,144},	{144,145,145},	{145,146,146},	{146,147,147},	{147,148,148},
			{148,148,149},	{149,149,150},	{150,150,151},	{151,151,152},	{152,152,153},	{153,153,154},
			{154,154,155},	{155,155,156},	{156,156,157},	{157,157,158},	{158,158,159},	{159,159,160},
			{160,160,161},	{161,161,162},	{162,162,163},	{163,163,164},	{164,164,165},	{165,165,166},
			{166,166,167},	{167,167,168},	{168,168,169},	{169,169,170},	{170,170,171},	{171,171,172},
			{172,172,173},	{173,173,174},	{174,174,175},	{175,175,176},	{176,176,177},	{177,177,178},
			{178,178,179},	{179,179,180},	{180,180,181},	{181,181,182},	{182,182,183},	{183,183,184},
			{184,184,185},	{185,185,186},	{186,186,187},	{187,187,188},	{188,188,189},	{189,189,190},
			{191,190,192},	{192,191,193},	{193,192,194},	{194,193,195},	{195,194,196},	{196,195,197},
			{197,196,198},	{198,197,199},	{199,198,200},	{200,199,201},	{201,200,202},	{203,201,203},
			{204,202,204},	{205,203,205},	{206,204,206},	{207,205,207},	{208,206,208},	{209,207,209},
			{210,208,210},	{211,209,211},	{212,210,212},	{212,210,212},	{213,211,213},	{214,212,214},
			{215,213,215},	{216,214,216},	{217,215,217},	{218,216,218},	{219,217,219},	{220,218,220},
			{221,219,221},	{222,220,222},	{223,221,223},	{224,222,223},	{225,223,224},	{226,224,225},
			{227,225,226},	{228,226,227},	{229,227,228},	{230,228,229},	{231,229,230},	{232,230,231},
			{233,231,232},	{234,232,233},	{235,233,234},	{236,234,235},	{237,235,236},	{238,236,237},
			{239,237,238},	{240,238,239},	{241,239,240},	{242,240,241},	{243,241,242},	{244,242,243},
			{245,243,243},	{246,244,244},	{247,245,245},	{248,246,246},	{249,247,247},	{250,248,248},
			{251,249,249},	{252,250,250},	{253,251,251},	{254,252,252},
		},
		*/
			{
				{0,0,0},    {1,1,1},     {2,2,2},      {3,3,3},      {4,4,4},         {5,5,5},
				{6,6,6},    {7,7,7},     {8,8,8},      {9,9,9},      {10,10,10},      {11,11,11},
				{12,12,12}, {13,13,13},  {14,14,14},   {15,15,15},   {16,16,16},      {17,17,17},
				{18,18,18}, {19,19,19},  {20,20,20},   {21,21,21},   {22,22,22},     {23,23,23},
				{24,24,24},	{25,25,25},	{26,26,26},	{27,27,27},	{28,28,28},	{29,29,29},
				{30,30,30},	{31,31,31},	{32,32,32},	{33,33,33},	{34,34,34},	{35,35,35},
				{36,36,36},	{37,37,37},	{38,38,38},	{39,39,39},	{40,40,40},	{41,41,41},
				{42,42,42},	{42,42,42},	{43,43,43},	{44,44,44},	{45,45,45},	{46,46,46},
				{47,47,47},	{48,48,48},	{49,49,49},	{50,50,50},	{51,51,51},	{52,52,52},
				{53,53,53},	{54,54,54},	{55,55,55},	{56,56,56},	{57,57,57},	{58,58,58},
				{59,59,59},	{60,60,60},	{61,61,61},	{62,62,62},	{63,63,63},	{64,64,64},
				{65,65,65},	{66,66,66},	{67,67,67},	{68,68,68},	{69,79,69},	{70,70,70},
				{71,71,71},	{72,72,72},	{73,73,73},	{74,74,74},	{75,75,75},	{76,76,76},
				{77,77,77},	{78,78,78},	{79,79,79},	{80,80,80},	{81,81,81},	{82,82,82},
				{83,83,83},	{84,84,84},	{85,85,85},	{86,86,86},	{87,87,87},	{88,88,88},
				{89,89,89},	{90,90,90},	{91,91,91},	{92,92,92},	{93,93,93},	{94,94,94},
				{95,95,95},	{96,96,96},	{97,97,97},	{98,98,98},	{99,99,99},	{100,100,100},
				{101,101,101},	{102,102,102},	{103,103,103},	{104,104,104},	{105,105,105},	{106,106,106},
				{107,107,107},	{108,108,108},	{109,109,109},	{110,110,110},	{111,111,111},	{112,112,112},
				{113,113,113},	{114,114,114},	{115,115,115},	{116,116,116},	{117,117,117},	{118,118,118},
				{119,119,119},	{120,120,120},	{121,121,121},	{122,122,122},	{123,123,123},	{124,124,124},
				{125,125,125},	{126,126,126},	{127,127,127},	{128,128,128},	{129,129,129},	{130,130,130},
				{131,131,131},	{132,132,132},	{133,133,133},	{134,134,134},	{135,135,135},	{136,136,136},
				{137,137,137},	{138,138,138},	{139,139,139},	{140,140,140},	{141,141,141},	{142,142,142},
				{143,143,143},	{144,144,144},	{145,145,145},	{146,146,146},	{147,147,147},	{148,148,148},
				{149,149,149},	{150,150,150},	{151,151,151},	{152,152,152},	{153,153,153},	{154,154,154},
				{155,155,155},	{156,156,156},	{157,157,157},	{158,158,158},	{159,159,159},	{160,160,160},
				{161,161,161},	{162,162,162},	{163,163,163},	{164,164,164},	{165,165,165},	{166,166,166},
				{167,167,167},	{168,168,168},	{169,169,169},	{170,170,170},	{171,171,171},	{172,172,172},
				{173,173,173},	{174,174,174},	{175,175,175},	{176,176,176},	{177,177,177},	{178,178,178},
				{179,179,179},	{180,180,180},	{181,181,181},	{182,182,182},	{183,183,183},	{184,184,184},
				{185,185,185},	{186,186,186},	{187,187,187},	{188,188,188},	{189,189,189},	{190,190,190},
				{191,191,191},	{192,192,192},	{193,193,193},	{194,194,194},	{195,195,195},	{196,196,196},
				{197,197,197},	{198,198,198},	{199,199,199},	{200,200,200},	{201,201,201},	{202,202,202},
				{203,203,203},	{204,204,204},	{205,205,205},	{206,206,206},	{207,207,207},	{208,208,208},
				{209,209,209},	{210,210,210},	{211,211,211},	{212,212,212},	{213,213,213},	{214,214,214},
				{215,215,215},	{216,216,216},	{217,217,217},	{218,218,218},	{219,219,219},	{220,220,220},
				{221,221,221},	{222,222,222},	{223,223,223},	{224,224,224},	{225,225,225},	{226,226,226},
				{227,227,227},	{228,228,228},	{229,229,229},	{230,230,230},	{231,231,231},	{232,232,232},
				{233,233,233},	{234,234,234},	{235,235,235},	{236,236,236},	{237,237,237},	{238,238,238},
				{239,239,239},	{240,240,240},	{241,241,241},	{242,242,242},	{243,243,243},	{244,244,244},
				{245,245,245},	{246,246,246},	{247,247,247},	{248,248,248},	{249,249,249},	{250,250,250},
				{251,251,251},	{252,252,252},	{253,253,253},	{254,254,254},
			},
		{
			////��--��--�ٺ�
			{243,255,255},{241,255,255},{240,255,255},{238,255,255},{237,255,255},{235,255,255},
			{234,255,255},{232,255,255},{231,255,255},{229,255,255},{227,255,255},{226,255,255},
			{224,255,255},{223,255,255},{221,255,255},{220,255,255},{218,255,255},{217,255,255},
			{215,255,255},{214,255,255},{212,255,255},{210,255,255},{209,255,255},{207,255,254},
			{206,255,251},{204,255,246},{203,255,243},{201,255,240},{200,255,237},{198,255,233},
			{196,255,229},{195,255,227},{193,255,223},{192,255,220},{190,255,216},{189,255,213},
			{187,255,210},{186,255,207},{184,255,203},{183,255,200},{181,255,197},{179,252,194},
			{178,251,191},{176,248,187},{175,247,185},{173,244,181},{172,243,179},{170,240,175},
			{169,239,172},{167,236,169},{166,235,167},{165,233,165},{164,231,163},{164,229,162},
			{164,228,161},{164,226,160},{165,225,159},{164,223,157},{164,221,156},{164,220,155},
			{164,218,154},{164,217,153},{163,215,151},{162,213,150},{162,212,149},{161,210,148},
			{161,209,147},{160,206,145},{160,205,144},{160,204,143},{160,202,142},{160,201,141},
			{159,198,139},{159,197,138},{159,196,137},{158,194,136},{158,193,135},{157,190,133},
			{157,189,132},{156,187,131},{156,186,130},{156,185,129},{155,182,127},{154,181,126},
			{154,179,125},{154,178,124},{154,177,123},{152,174,121},{152,173,120},{151,171,119},
			{151,170,118},{149,168,117},{148,166,115},{148,165,114},{147,163,113},{147,162,112},
			{146,160,111},{145,158,109},{145,157,108},{144,155,107},{144,154,106},{143,152,105},
			{141,150,103},{140,148,102},{140,147,101},{140,146,100},{139,144,99}, {138,143,98},
			{137,142,97}, {136,140,96}, {136,139,95}, {135,138,94}, {135,137,94}, {135,136,93},
			{134,135,92}, {132,133,91}, {132,132,90}, {131,130,89}, {129,128,88}, {128,127,87},
			{127,125,86}, {125,122,85}, {124,121,84}, {123,120,83}, {121,117,82}, {120,115,81},
			{119,114,80}, {117,112,79}, {117,111,79}, {116,110,78}, {114,107,77}, {113,106,76},
			{112,105,75}, {110,102,74}, {109,101,73}, {108,99,72},  {106,97,71},  {105,96,70},
			{104,94,69},  {102,92,68},  {101,91,67},  {100,89,66},  {98,87,65},   {97,85,64},
			{97,85,64},   {96,84,63},   {94,82,62},   {93,80,61},   {92,79,60},   {90,77,59},
			{89,76,58},   {88,74,57},   {86,72,56},   {85,71,55},   {84,70,54},   {82,67,53},
			{81,66,52},   {80,65,51},   {78,63,50},   {77,62,49},   {77,61,49},   {75,60,48},
			{74,58,47},   {73,57,46},   {71,55,45},   {70,54,44},   {69,53,43},   {67,51,42},
			{66,49,41},   {65,48,40},   {63,47,39},   {62,45,38},   {61,44,37},   {59,43,36},
			{58,41,35},   {57,40,34},   {56,39,34},   {55,38,33},   {54,37,32},   {52,35,31},
			{51,34,30},   {50,33,29},   {48,31,28},   {47,30,27},   {48,31,28},   {48,31,28},
			{48,31,28},   {49,32,29},   {51,34,30},   {51,34,30},   {51,34,30},   {52,35,31},
			{53,36,32},   {53,36,32},   {53,36,32},   {54,37,33},   {55,39,34},   {56,39,34},
			{56,39,34},   {57,40,35},   {58,41,36},   {58,41,36},   {255,246,0},  {255,242,0},
			{255,238,0},  {255,234,0},  {255,230,0},  {255,225,0},  {255,217,0},  {255,212,0},
			{255,208,0},  {255,204,0},  {255,200,0},  {255,196,0},  {255,191,0},  {255,187,0},
			{255,183,0},  {255,178,0},  {255,170,0},  {255,166,0},  {255,162,0},  {255,157,0},
			{255,153,0},  {255,149,0},  {255,144,0},  {255,140,0},  {255,136,0},  {255,132,0},
			{255,128,0},  {255,119,0},  {255,115,0},  {255,110,0},  {255,106,0},  {255,102,0},
			{255,98,0},   {255,94,0},   {255,89,0},   {255,85,0},   {255,81,0},   {255,76,0},
			{255,68,0},   {255,64,0},   {255,60,0},   {255,55,0},   {255,51,0},   {255,47,0},
			{255,42,0},   {255,38,0},   {255,34,0},   {255,30,0},   {255,21,0},   {255,17,0},
			{255,13,0},   {255,8,0},    {255,4,0},    {255,0,0},
		},
			////��--��--��
		{
			{246,234,0},  {244,232,0},  {243,231,1},  {241,225,1},  {239,223,1},  {237,221,1},
			{236,220,2},  {234,215,2},  {232,213,2},  {230,211,2},  {229,210,3},  {227,205,3},
			{225,203,3},  {223,201,3},  {222,200,4},  {220,195,4},  {218,193,4},  {216,191,4},
			{215,191,5},  {213,185,5},  {211,184,5},  {209,182,5},  {208,181,6},  {206,176,6},
			{204,174,6},  {202,173,6},  {201,172,7},  {199,167,7},  {197,165,7},  {195,164,7},
			{194,163,8},  {192,158,8},  {190,157,8},  {189,156,9},  {187,154,9},  {185,150,9},
			{183,148,9},  {182,148,10}, {180,146,10}, {178,142,10}, {176,140,10}, {175,139,11},
			{173,138,11}, {171,134,11}, {169,132,11}, {168,132,12}, {166,130,12}, {164,126,12},
			{162,124,12}, {161,124,13}, {159,123,13}, {157,119,13}, {155,117,13}, {154,117,14},
			{152,115,14}, {150,111,14}, {148,110,14}, {147,110,15}, {145,108,15}, {143,105,15},
			{142,104,16}, {140,103,16}, {138,101,16}, {136,98,16},  {135,98,17},  {133,96,17},
			{131,95,17},  {129,92,17},  {128,91,18},  {126,90,18},  {124,89,18},  {122,86,18},
			{121,85,19},  {119,84,19},  {117,83,19},  {115,80,19},  {114,80,20},  {112,78,20},
			{110,77,20},  {108,74,20},  {107,74,21},  {105,73,21},  {103,72,21},  {101,69,21},
			{100,69,22},  {98,68,22},   {96,66,22},   {94,64,22},   {93,64,23},   {91,63,23},
			{130,154,164},{237,237,237},{234,234,234},{232,232,232},{229,229,229},{226,226,226},
			{224,224,224},{221,221,221},{218,218,218},{216,216,216},{213,213,213},{210,210,210},
			{208,208,208},{205,205,205},{202,202,202},{200,200,200},{197,197,197},{194,194,194},
			{192,192,192},{189,189,189},{186,186,186},{184,184,184},{181,181,181},{178,178,178},
			{175,175,175},{173,173,173},{170,170,170},{167,167,167},{165,165,165},{162,162,162},
			{159,159,159},{157,157,157},{154,154,154},{151,151,151},{149,149,149},{146,146,146},
			{143,143,143},{141,141,141},{138,138,138},{135,135,135},{133,133,133},{130,130,130},
			{127,127,127},{125,125,125},{122,122,122},{119,119,119},{117,117,117},{114,114,114},
			{111,111,111},{109,109,109},{106,106,106},{103,103,103},{101,101,101},{98,98,98},
			{95,95,95},   {93,93,93},   {90,90,90},   {87,87,87},   {85,85,85},   {82,82,82},
			{79,79,79},   {77,77,77},   {74,74,74},   {71,71,71},   {68,68,68},   {66,66,66},
			{63,63,63},   {60,60,60},   {58,58,58},   {55,55,55},   {52,52,52},   {50,50,50},
			{47,47,47},   {44,44,44},   {42,42,42},   {39,39,39},   {36,36,36},   {34,34,34},
			{31,31,31},   {28,28,28},   {26,26,26},   {23,23,23},   {20,20,20},   {18,18,18},
			{15,15,15},   {119,103,147},{191,255,255},{189,255,255},{186,255,254},{184,255,254},
			{181,255,253},{179,255,252},{176,255,251},{174,255,251},{172,255,252},{169,255,251},
			{167,255,251},{164,255,250},{162,255,250},{160,253,250},{157,250,248},{155,249,249},
			{152,244,246},{150,241,244},{147,237,242},{145,234,240},{143,231,239},{140,226,236},
			{138,223,234},{135,219,232},{133,214,230},{131,210,228},{128,206,226},{126,203,224},
			{123,199,222},{121,195,220},{118,191,217},{116,188,216},{114,184,214},{111,180,212},
			{109,176,210},{106,172,207},{104,169,206},{102,165,204},{99,160,201}, {97,157,200},
			{94,151,197}, {92,147,196}, {89,143,193}, {87,139,191}, {85,136,190}, {82,131,187},
			{80,127,185}, {77,123,183}, {75,119,181}, {73,116,180}, {70,111,177}, {68,107,175},
			{65,103,173}, {63,99,171},  {60,95,169},  {58,91,167},  {56,85,165},  {53,81,163},
			{51,77,161},  {48,72,158},  {46,68,157},  {44,64,155},  {41,60,153},  {39,56,151},
			{36,51,148},  {34,47,147},  {31,42,144},  {29,38,142},  {27,35,141},  {24,30,138},
			{22,26,137},  {19,21,134},  {19,17,132},  {19,15,131},  {18,12,128},  {18,10,126},
			{17,7,124},   {17,5,122},   {16,2,120},   {16,0,118},
		},
			////��--��--��
			/*
			{255,239,239},
			{255,238,238},
			{255,237,237},
			{255,236,236},
			{255,236,236},
			{255,235,235},
			{255,234,234},
			{255,233,233},
			{255,232,232},
			{255,231,231},
			{255,230,230},
			{255,229,229},
			{255,228,228},
			{255,228,228},
			{255,227,227},
			{255,226,226},
			{255,225,225},
			{255,224,224},
			{255,223,223},
			{255,222,222},
			{255,222,222},
			{255,221,221},
			{255,220,220},
			{255,219,219},
			{255,218,218},
			{255,217,217},
			{255,216,216},
			{255,215,215},
			{255,214,214},
			{255,214,214},
			{255,213,213},
			{255,212,212},
			{255,211,211},
			{255,210,210},
			{255,209,209},
			{255,208,208},
			{255,207,207},
			{255,206,206},
			{255,205,205},
			{255,204,204},
			{255,203,203},
			{255,202,202},
			{255,202,202},
			{255,201,201},
			{255,200,200},
			{255,199,199},
			{255,198,198},
			{255,197,197},
			{255,196,196},
			{255,195,195},
			{255,194,194},
			{255,193,193},
			{255,192,192},
			{255,191,191},
			{255,190,190},
			{255,189,189},
			{255,188,188},
			{255,187,187},
			{255,186,186},
			{255,185,185},
			{255,184,184},
			{255,183,183},
			{255,183,183},
			{255,182,182},
			{255,181,181},
			{255,180,180},
			{255,179,179},
			{255,178,178},
			{255,177,177},
			{255,176,176},
			{255,175,175},
			{255,174,174},
			{255,173,173},
			{255,172,172},
			{255,171,171},
			{255,170,170},
			{255,169,169},
			{255,168,168},
			{255,167,167},
			{255,166,166},
			{255,165,165},
			{255,164,164},
			{255,164,164},
			{255,163,163},
			{255,162,162},
			{255,161,161},
			{255,160,160},
			{255,159,159},
			{255,158,158},
			{255,157,157},
			{255,156,156},
			{255,155,155},
			{255,154,154},
			{255,153,153},
			{255,152,152},
			{255,151,151},
			{255,150,150},
			{255,149,149},
			{255,148,148},
			{255,147,147},
			{255,146,146},
			{255,145,145},
			{255,145,145},
			{255,144,144},
			{255,143,143},
			{255,142,142},
			{255,141,141},
			{255,140,140},
			{255,139,139},
			{255,138,138},
			{255,137,137},
			{255,136,136},
			{255,135,135},
			{255,134,134},
			{255,133,133},
			{255,132,132},
			{255,131,131},
			{255,130,130},
			{255,129,129},
			{253,128,128},
			{251,127,127},
			{249,126,126},
			{248,126,126},
			{246,125,125},
			{244,124,124},
			{242,123,123},
			{240,122,122},
			{238,121,121},
			{236,120,120},
			{234,119,119},
			{232,118,118},
			{230,117,117},
			{228,116,116},
			{224,114,114},
			{222,113,113},
			{220,112,112},
			{217,111,111},
			{214,109,109},
			{212,108,108},
			{209,107,107},
			{206,105,105},
			{204,104,104},
			{201,103,103},
			{198,101,101},
			{196,100,100},
			{193,99,99},
			{191,98,98},
			{188,96,96},
			{185,95,95},
			{183,94,94},
			{180,92,92},
			{177,91,91},
			{175,90,90},
			{173,89,89},
			{169,87,87},
			{167,86,86},
			{165,85,85},
			{161,83,83},
			{159,82,82},
			{157,81,81},
			{154,80,80},
			{151,79,78},
			{149,78,77},
			{147,77,76},
			{143,75,74},
			{141,74,73},
			{139,73,72},
			{135,71,70},
			{133,70,69},
			{131,69,68},
			{128,68,67},
			{125,66,65},
			{123,65,64},
			{120,64,63},
			{117,62,61},
			{115,61,60},
			{112,60,59},
			{110,59,58},
			{107,57,56},
			{104,56,55},
			{102,55,54},
			{99,53,52},
			{96,52,51},
			{94,51,50},
			{91,49,48},
			{88,48,47},
			{86,47,46},
			{84,46,45},
			{80,44,43},
			{78,43,42},
			{78,43,42},
			{76,42,41},
			{76,42,41},
			{74,41,40},
			{74,41,40},
			{72,40,39},
			{72,40,39},
			{70,39,38},
			{70,39,38},
			{68,38,37},
			{68,38,37},
			{67,38,37},
			{66,38,36},
			{65,37,36},
			{64,36,35},
			{63,36,35},
			{62,35,34},
			{61,35,34},
			{60,34,33},
			{59,34,33},
			{58,34,32},
			{57,34,32},
			{57,34,32},
			{55,33,31},
			{55,33,31},
			{53,32,30},
			{53,32,30},
			{51,30,29},
			{51,31,29},
			{49,30,28},
			{49,30,28},
			{47,29,27},
			{47,29,27},
			{47,29,27},
			{45,28,26},
			{45,28,26},
			{43,27,25},
			{43,27,25},
			{41,26,24},
			{41,26,24},
			{39,25,23},
			{39,25,23},
			{37,24,22},
			{37,24,22},
			{36,23,22},
			{35,23,21},
			{34,23,21},
			{33,22,20},
			{32,21,20},
			{31,20,19},
			{30,20,19},
			{29,19,18},
			{28,19,18},
			{27,18,17},
			{26,18,17},
			{26,18,17},
			{24,17,16},
			{24,17,16},
			{22,16,15},
			{22,16,15},
			{20,15,14},
			{20,15,14},
			{18,14,13},
			{18,14,13},
			{16,13,12},
			{16,13,12},
			*/
			/*
			//////��--��
			{114,130,211},
			{114,130,211},
			{113,129,210},
			{113,129,211},
			{113,129,211},
			{112,128,210},
			{112,128,210},
			{112,128,210},
			{112,128,210},
			{111,127,210},
			{111,127,210},
			{111,127,210},
			{110,126,209},
			{110,126,209},
			{110,126,209},
			{109,126,209},
			{109,126,209},
			{109,126,209},
			{108,125,208},
			{108,125,208},
			{108,125,208},
			{108,125,209},
			{107,122,208},
			{107,122,208},
			{107,122,208},
			{106,121,207},
			{106,121,207},
			{106,121,208},
			{105,120,207},
			{105,120,207},
			{105,120,207},
			{105,120,207},
			{104,119,207},
			{104,119,207},
			{104,119,207},
			{103,118,206},
			{103,118,206},
			{103,118,206},
			{102,118,206},
			{102,118,206},
			{102,118,206},
			{101,117,205},
			{101,117,205},
			{101,117,205},
			{101,117,206},
			{100,116,205},
			{100,116,205},
			{100,116,205},
			{99,115,204},
			{99,115,204},
			{99,115,205},
			{98,114,204},
			{98,114,204},
			{98,114,204},
			{97,113,203},
			{97,113,203},
			{97,113,204},
			{97,113,204},
			{96,112,203},
			{96,112,203},
			{96,112,203},
			{95,111,203},
			{95,111,203},
			{95,111,203},
			{94,108,202},
			{94,108,202},
			{94,108,202},
			{94,109,203},
			{93,108,202},
			{93,108,202},
			{93,108,202},
			{92,107,201},
			{92,107,201},
			{92,107,202},
			{91,106,201},
			{91,106,201},
			{91,106,201},
			{90,105,200},
			{90,105,200},
			{90,105,201},
			{90,105,201},
			{89,104,200},
			{89,104,200},
			{89,104,200},
			{88,103,199},
			{88,103,200},
			{88,103,200},
			{87,102,199},
			{87,102,199},
			{87,102,199},
			{86,101,199},
			{86,101,199},
			{86,101,199},
			{86,101,199},
			{85,100,198},
			{85,100,198},
			{85,100,199},
			{84,99,198},
			{84,99,198},
			{84,99,198},
			{83,98,197},
			{83,98,197},
			{83,98,198},
			{82,97,197},
			{82,97,197},
			{82,97,197},
			{82,97,197},
			{81,94,196},
			{81,95,197},
			{81,95,197},
			{80,94,196},
			{80,94,196},
			{80,94,196},
			{79,93,195},
			{79,93,196},
			{79,93,196},
			{79,93,196},
			{78,92,195},
			{78,92,195},
			{78,92,196},
			{77,91,195},
			{77,91,195},
			{77,91,195},
			{76,90,194},
			{76,90,194},
			{76,90,195},
			{75,89,194},
			{75,89,194},
			{75,89,194},
			{75,89,194},
			{74,88,193},
			{74,88,194},
			{74,88,194},
			{73,87,193},
			{73,87,193},
			{73,87,193},
			{72,86,192},
			{72,86,193},
			{72,86,193},
			{71,85,192},
			{71,85,192},
			{71,85,192},
			{71,85,193},
			{70,84,192},
			{70,84,192},
			{70,84,192},
			{69,83,191},
			{69,83,191},
			{69,83,192},
			{68,80,191},
			{68,80,191},
			{68,80,191},
			{68,80,191},
			{67,79,190},
			{67,79,191},
			{67,79,191},
			{66,78,190},
			{66,78,190},
			{66,78,190},
			{65,77,189},
			{65,77,190},
			{65,77,190},
			{64,76,189},
			{64,76,189},
			{64,76,189},
			{64,76,189},
			{63,76,189},
			{63,76,189},
			{63,76,189},
			{62,75,188},
			{62,75,188},
			{62,75,189},
			{61,74,188},
			{61,74,188},
			{61,74,188},
			{60,73,187},
			{60,73,187},
			{60,73,188},
			{60,73,188},
			{59,72,187},
			{59,72,187},
			{59,72,187},
			{58,71,186},
			{58,71,187},
			{58,71,187},
			{57,70,186},
			{57,70,186},
			{57,70,186},
			{56,69,185},
			{56,69,186},
			{56,69,186},
			{56,69,186},
			{55,66,185},
			{55,66,185},
			{55,66,185},
			{54,65,185},
			{54,65,185},
			{54,65,185},
			{53,64,184},
			{53,64,184},
			{53,64,185},
			{53,64,185},
			{52,63,184},
			{52,63,184},
			{52,63,184},
			{51,62,183},
			{51,62,184},
			{51,62,184},
			{50,61,183},
			{50,61,183},
			{50,61,183},
			{49,60,182},
			{49,60,183},
			{49,60,183},
			{49,60,183},
			{48,59,182},
			{48,59,182},
			{48,59,182},
			{47,58,182},
			{47,58,182},
			{47,58,182},
			{46,57,181},
			{46,57,181},
			{46,57,181},
			{45,56,181},
			{45,56,181},
			{45,56,181},
			{45,56,181},
			{44,55,180},
			{44,55,181},
			{44,55,181},
			{43,54,180},
			{43,54,180},
			{43,54,180},
			{42,51,179},
			{42,51,180},
			{42,51,180},
			{42,51,180},
			{41,50,179},
			{41,50,179},
			{41,50,179},
			{40,49,179},
			{40,49,179},
			{40,49,179},
			{39,48,178},
			{39,48,178},
			{39,48,178},
			{38,47,178},
			{38,47,178},
			{38,47,178},
			{38,47,178},
			{37,46,177},
			{37,46,177},
			{37,46,178},
			{36,45,177},
			{36,45,177},
			*/
	};
	//
	public int getColorNumber()
	{
//		if(mCurrentColPalIndex ==0 )
//			return 30;
		if(mCurrentColPalIndex<mHighColpalNumber)
			return mColNumber;
		else
			return mMiddleColNumber;
	}
	public int[][] getColors()
	{
//		if(mCurrentColPalIndex == 0 )
//			return mLowColPal[0];
		
		if(mCurrentColPalIndex<mHighColpalNumber)
			return mColPal[mCurrentColPalIndex];
		else
			return mMiddleColPal[mCurrentColPalIndex-mHighColpalNumber];
	}
	//
	public int getColpalIndex()
	{
		return mCurrentColPalIndex;
	}
	public void setColorpalIndex(int index)
	{
		mCurrentColPalIndex=index;
	}
	public void setMiddleColorpalIndex(int index)
	{
		mCurrentColPalIndex = index+mHighColpalNumber;
	}
	public int getBackColor()
	{
		return mBackColor;
	}
	public int getFillColor()
	{
		return mFillColor;
	}
}